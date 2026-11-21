# Code Quantum

Application Java qui charge un problème QUBO et le résout avec Google OR-Tools CP-SAT. Le projet reste volontairement léger : un modèle métier canonique, un lecteur par format, une stratégie de construction de modèle, puis un composant de résolution.

## Prérequis et compilation

- JDK 27
- Maven 3.9 ou supérieur
- Lombok (traitement d'annotations configuré par Maven) et Logback (journalisation)

Compiler et exécuter les tests :

```powershell
mvn test
```

## Exécution

Le fichier fourni se trouve dans `src/main/resources/qubo.json`. Depuis la racine du projet :

```powershell
mvn compile exec:java
```

Options disponibles :

```text
--input=CHEMIN       chemin disque ou ressource classpath (défaut : src/main/resources/qubo.json)
--format=json-matrix format d'entrée
--strategy=linearization stratégie CpModel
--max-decimal-places=N nombre maximal de décimales pour la conversion exacte (défaut : 9)
--time-limit-seconds=N limite de temps CP-SAT (défaut : 60)
--workers=N          nombre de workers CP-SAT (défaut : 1)
--logs=true|false    afficher les logs de recherche (défaut : false)
```

Les sorties de l'application et les logs de progression CP-SAT passent par SLF4J/Logback. Le niveau et le format des messages console sont configurés dans `src/main/resources/logback.xml` ; l'option `--logs=true` active les messages de recherche CP-SAT et les achemine vers Logback.

Exemple :

```powershell
mvn compile exec:java "-Dexec.args=--input=src/main/resources/qubo.json --time-limit-seconds=30 --workers=4"
```

## Format JSON

Le format d'entrée de cette application est une sérialisation JSON de la représentation matricielle QUBO illustrée dans les exemples Pasqal ; il ne s'agit pas d'un format JSON officiel de Pasqal. L'objet `qubo` contient uniquement la propriété `matrix` :

```json
{
  "qubo": {
    "matrix": [
      [-3, 1, 2],
      [1, -2, 3],
      [2, 3, -4]
    ]
  }
}
```

La dimension de la matrice détermine le nombre de variables, dont les indices sont les positions correspondantes. La matrice doit être non vide, carrée, symétrique et contenir uniquement des coefficients numériques finis. L'exemple du projet a pour optimum unique `x = [0, 0, 1]`, de coût `-4`.

## Convention mathématique et linéarisation

La fonction minimisée est exactement `f(x) = xᵀQx`, avec `x ∈ {0,1}ⁿ` et `Q` symétrique. Puisque `xi² = xi`, son développement est `Σᵢ Qᵢᵢxi + 2Σᵢ<ⱼ Qᵢⱼxᵢxⱼ`. Les coefficients de la diagonale sont linéaires ; chaque coefficient de la partie triangulaire supérieure est pris une seule fois puis multiplié par deux. Aucun offset ni normalisation implicite n'est appliqué.

CP-SAT n'acceptant que des objectifs linéaires entiers, chaque produit non nul `xᵢxⱼ` est remplacé par une variable binaire `yᵢⱼ` et les contraintes `yᵢⱼ ≤ xᵢ`, `yᵢⱼ ≤ xⱼ` et `yᵢⱼ ≥ xᵢ + xⱼ - 1`. Aucun auxiliaire n'est créé pour les diagonales ou les coefficients hors diagonale nuls.

La conversion de coefficients se fait avec la représentation décimale courte de chaque `double` (`BigDecimal.valueOf`). Le plus petit facteur puissance de dix qui rend tous les coefficients entiers est utilisé, dans la limite de `--max-decimal-places`. Une conversion non exacte ou un dépassement de capacité produit une erreur au lieu d'arrondir silencieusement. La somme des valeurs absolues des coefficients de l'objectif est limitée à `2^53` pour conserver exactement les objectifs entiers exposés comme `double` par OR-Tools. La mise à l'échelle ne change pas l'optimum mais les coûts et bornes sont ramenés à l'échelle d'origine dans le résultat.

## Architecture et extensions

- `qubo.QuboProblem` est le modèle métier canonique : validation, dimension, accès aux coefficients et évaluation directe de `xᵀQx`. Il ne dépend ni de Jackson ni d'OR-Tools.
- `reading.QuboReader` convertit une source en `QuboProblem`. `JsonMatrixQuboReader` est le premier adaptateur ; un lecteur CSV ou sparse pourra être ajouté sans modifier le modèle ou le solveur, puis sélectionné dans `InputFormat`.
- `cpsat.QuboCpModelFactory` isole la construction du modèle OR-Tools. `LinearizedQuboCpModelFactory` implémente la linéarisation ; une nouvelle stratégie peut implémenter cette interface puis être ajoutée à `ModelStrategy` sans toucher aux lecteurs ou au modèle métier.
- `solving.QuboSolver` sépare l'exécution de la construction. `CpSatQuboSolver` mappe les états CP-SAT vers un résultat indépendant d'OR-Tools (`QuboSolution`).
- `app.QuboApplication` relie configuration, lecture, construction et résolution ; la sélection se fait par les options de ligne de commande.

Les statuts distinguent `OPTIMAL`, `FEASIBLE`, `NO_SOLUTION` et `INTERRUPTED`. En l'absence de solution, le tableau des valeurs est vide et le coût est absent.
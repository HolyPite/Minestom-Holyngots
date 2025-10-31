# Minestom-Holyngots

Minestom-Holyngots est un serveur d'action-RPG construit sur le framework [Minestom](https://github.com/Minestom/Minestom). Le projet fournit une base complete pour creer un univers persistant : mondes multiples, progression de quetes, PNJ interactifs, objets personnalises et boucles de combat avancees. L'objectif est de proposer une experience modulaire et facilement extensible pour experimenter autour d'un gameplay MMO.

## Fonctionnalites principales

- **Instances de jeu et de build** : chargement de plusieurs mondes (`worlds/`) avec regroupements par type afin de distinguer les zones jouables des espaces de construction. La classe `InstancesInit` gere la creation, le mappage et l'exposition des instances Minestom.
- **Persistance des joueurs** : les donnees sont serialisees en JSON dans `playerdata/` via `PlayerDataService` et `JsonPlayerDataRepository`. Les statistiques, quetes, inventaires et positions sont automatiquement sauvegardes.
- **Systeme de quetes scenarisees** : quetes decoupees en etapes avec objectifs (deplacements, dialogues, eliminations, collecte, etc.), dialogues de PNJ, delais, conditions de niveau, recompenses et repetabilite. Le registre (`QuestRegistry`) et le gestionnaire (`QuestManager`) centralisent la progression et les evenements personnalises.
- **PNJ interactifs et interface livre** : le bootstrap (`NpcBootstrap`) instancie les PNJ et la `BookGuiManager` affiche dialogues, informations de quetes et menus contextuels.
- **Objets et inventaires personnalises** : bibliotheque d'objets (`ItemRegistry`, `GameItem`) avec raretes, categories, statistiques et comportements specifiques via `ItemEventsGlobal`/`ItemEventsCustom`.
- **Boucle de combat avancee** : gestion des historiques de degats, vitesse d'attaque, recul et feedback visuel via le module `org.example.mmo.combat`. Les evenements sont isoles dans des noeuds dedies pour ne declencher la logique que dans les instances de jeu.
- **Commandes utilitaires** : commandes Minestom pour teleporter les joueurs, gerer les quetes, distribuer des objets, stopper le serveur ou tester les interactions PNJ (`/teleport`, `/quests`, `/setquest`, `/removequest`, `/giveitem`, `/npcinteraction`, `/stop`, etc.).

## Structure du projet

```
src/main/java/org/example/
├── Main.java                 # Bootstrap du serveur Minestom
├── InstancesInit.java        # Creation et regroupement des instances
├── NodesManagement.java      # Arbre d'evenements et initialisation des systemes
├── commands/                 # Commandes serveur personnalisees
├── data/                     # Acces et persistance des donnees joueur
└── mmo/                      # Modules gameplay (quetes, PNJ, items, inventaires)
```

Les mondes Anvil se trouvent dans `worlds/` et peuvent etre remplaces par vos propres cartes. Les fichiers `playerdata/<uuid>.json` sont crees automatiquement pour chaque joueur.

## Prerequis

- Java 21 (Minestom requiert au minimum cette version)
- Aucune installation de Gradle n'est necessaire : le projet fournit le wrapper (`./gradlew`).

## Compilation

```bash
./gradlew build
```

La tache `build` genere une archive executable dans `build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar` (shadow jar inclus).

## Lancement du serveur

```bash
java -jar build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar
```

Par defaut le serveur ecoute sur `0.0.0.0:25565`. Les dependances etant integrees au shadow jar, aucun chemin de classe supplementaire n'est requis.

## Pour aller plus loin

- Ajoutez de nouvelles quetes en implementant `Quest`/`QuestStep` dans `org.example.mmo.quest.quests` puis en les enregistrant via `QuestBootstrap`.
- Declarez des PNJ supplementaires dans `NpcBootstrap` en leur associant dialogues, apparences et identifiants de quete.
- Creez des objets uniques en enrichissant `ItemBootstrap` et `ItemRegistry`.
- Exploitez les noeuds d'evenements (`NodesManagement`) pour brancher d'autres systemes (minijeux, metiers, etc.).

N'hesitez pas a forker le projet pour adapter les systemes a vos besoins et contribuer aux ameliorations !

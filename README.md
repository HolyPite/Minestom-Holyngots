# Minestom-Holyngots

Minestom-Holyngots est un serveur d'action-RPG construit sur le framework [Minestom](https://github.com/Minestom/Minestom). Le projet fournit une base complète pour créer un univers persistant : mondes multiples, progression de quêtes, PNJ interactifs, objets personnalisés et boucles de combat avancées. L'objectif est de proposer une expérience modulaire et facilement extensible pour expérimenter autour d'un gameplay MMO.

## Fonctionnalités principales

- **Instances de jeu et de build** : chargement de plusieurs mondes (`worlds/`) avec regroupements par type afin de distinguer les zones jouables des espaces de construction. La classe `InstancesInit` gère la création, le mappage et l'exposition des instances Minestom.
- **Persistance des joueurs** : les données sont sérialisées en JSON dans `playerdata/` via `PlayerDataService` et `JsonPlayerDataRepository`. Les statistiques, quêtes, inventaires et positions sont automatiquement sauvegardés.
- **Système de quêtes scénarisées** : quêtes découpées en étapes avec objectifs (déplacements, dialogues, éliminations, collecte, etc.), dialogues de PNJ, délais, conditions de niveau, récompenses et répétabilité. Le registre (`QuestRegistry`) et le gestionnaire (`QuestManager`) centralisent la progression et les événements personnalisés.
- **PNJ interactifs et interface livre** : le bootstrap (`NpcBootstrap`) instancie les PNJ et la `BookGuiManager` affiche dialogues, informations de quêtes et menus contextuels.
- **Objets et inventaires personnalisés** : bibliothèque d'objets (`ItemRegistry`, `GameItem`) avec raretés, catégories, statistiques et comportements spécifiques via `ItemEventsGlobal`/`ItemEventsCustom`.
- **Boucle de combat avancée** : gestion des historiques de dégâts, vitesse d'attaque, recul et feedback visuel via le module `org.example.mmo.combat`. Les événements sont isolés dans des nœuds dédiés pour ne déclencher la logique que dans les instances de jeu.
- **Commandes utilitaires** : commandes Minestom pour téléporter les joueurs, gérer les quêtes, distribuer des objets, stopper le serveur ou tester les interactions PNJ (`/teleport`, `/quests`, `/setquest`, `/removequest`, `/giveitem`, `/npcinteraction`, `/stop`, etc.).

## Structure du projet

```
src/main/java/org/example/
├── Main.java                 # Bootstrap du serveur Minestom
├── InstancesInit.java        # Création et regroupement des instances
├── NodesManagement.java      # Arbre d'événements et initialisation des systèmes
├── commands/                 # Commandes serveur personnalisées
├── data/                     # Accès et persistance des données joueur
└── mmo/                      # Modules gameplay (combat, quêtes, PNJ, items, inventaires)
```

Les mondes Anvil se trouvent dans `worlds/` et peuvent être remplacés par vos propres cartes. Les fichiers `playerdata/<uuid>.json` sont créés automatiquement pour chaque joueur.

## Prérequis

- Java 21 (Minestom requiert au minimum cette version)
- Aucune installation de Gradle n'est nécessaire : le projet fournit le wrapper (`./gradlew`).

## Compilation

```bash
./gradlew build
```

La tâche `build` génère une archive exécutable dans `build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar` (shadow jar inclus).

## Lancement du serveur

```bash
java -jar build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar
```

Par défaut le serveur écoute sur `0.0.0.0:25565`. Les dépendances étant intégrées au shadow jar, aucun chemin de classe supplémentaire n'est requis.

## Pour aller plus loin

- Ajoutez de nouvelles quêtes en implémentant `Quest`/`QuestStep` dans `org.example.mmo.quest.quests` puis en les enregistrant via `QuestBootstrap`.
- Déclarez des PNJ supplémentaires dans `NpcBootstrap` en leur associant dialogues, apparences et identifiants de quête.
- Créez des objets uniques en enrichissant `ItemBootstrap` et `ItemRegistry`.
- Exploitez les nœuds d'événements (`NodesManagement`) pour brancher d'autres systèmes (minijeux, métiers, etc.).

N'hésitez pas à forker le projet pour adapter les systèmes à vos besoins et contribuer aux améliorations !

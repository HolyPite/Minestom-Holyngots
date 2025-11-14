# Advancement UI Tooling

Outils prévus pour exploiter l’écran d’advancements vanilla exclusivement comme surface d’affichage des arbres de compétences.

## 1. Contexte & Contraintes
- L’API Minestom expose la hiérarchie (`AdvancementManager`, `AdvancementTab`, `Advancement`, `AdvancementRoot` dans `net.minestom.server.advancements`) mais ne remonte aucun clic interne : seul `AdvancementTabEvent` (package `net.minestom.server.event.player`) signale l’ouverture/fermeture.
- Un `AdvancementTab` partage son état entre tous les viewers (`AdvancementTab` maintient `PLAYER_TAB_MAP`). Pour un rendu personnalisé, chaque joueur doit recevoir sa propre copie afin d’éviter la progression partagée.
- Les changements de titre/description/icône/position déclenchent automatiquement un renvoi de packet (`Advancement.update()` reconstruit les critères et envoie `removePacket` puis `createPacket()`).

## 2. Formats de définition
- Format JSON situé dans `src/main/resources/skilltrees/` (ex: `basic_combat.json`) :
  ```json
  {
    "id": "basic_combat",
    "display": {
      "title": "Combat Démo",
      "description": "Arbre factice pour tester le rendu Advancement",
      "icon": "IRON_SWORD",
      "frameType": "TASK",
      "x": 0.0,
      "y": 0.0,
      "background": "minecraft:textures/gui/advancements/backgrounds/story.png" // utiliser l'un des fonds vanilla: adventure|husbandry|nether|story|the_end
    },
    "nodes": [
      {
        "id": "strike_i",
        "title": "Frappe I",
        "description": "+1 dégâts de mêlée",
        "icon": "IRON_SWORD",
        "frameType": "TASK",
        "toast": true,
        "children": [
          {
            "id": "strike_ii",
            "title": "Frappe II",
            "icon": "IRON_SWORD",
            "frameType": "GOAL",
            "toast": true,
            "children": [
              { "id": "berserk", "title": "Frénésie", "icon": "GOLDEN_SWORD", "frameType": "TASK" }
            ]
          }
        ]
      }
    ],
    "layout": {
      "autoArrange": true,
      "horizontalStep": 2.5,
      "verticalSpacing": 1.5
    }
  }
  ```
- DTO Java dédiés (`org.example.mmo.dev.advancementui.definition.*`) :
  - `SkillTreeDefinition` : id + `SkillTreeDisplayDefinition display` + `List<SkillNodeDefinition> nodes` + `SkillTreeLayoutConstraints layout`.
  - `SkillNodeDefinition` : id, titres, icône (`Material`), `frameType`, `toast/hidden`, `secret` (si vrai le nœud n’est pas créé tant qu’on ne l’expose pas manuellement), `metadata` libre et `children` (déclaration imbriquée des compétences filles). Le `parentId` reste disponible pour les cas particuliers, mais l’arbre est normalement déduit de la hiérarchie.
  - `SkillTreeLayoutConstraints` : bornes X/Y, distance minimale et options `autoArrange` (disposition automatique) + `horizontalStep` (écart entre niveaux) + `verticalSpacing` (espacement entre branches d’un même niveau).

## 3. `AdvancementGraphBuilder`
Package suggéré : `org.example.mmo.dev.advancementui`.

Responsabilités :
- Lire une `SkillTreeDefinition` et produire :
  - un `AdvancementRoot` (title, description, icon, frame, background) ;
  - un graphe d’`Advancement` enfants reliés via `AdvancementTab.createAdvancement`.
- Vérifier que chaque parent existe et que les coordonnées respectent les contraintes (lever une exception descriptive sinon). Si `autoArrange=true`, un layout horizontal est calculé automatiquement : chaque niveau est repoussé vers la droite (`horizontalStep`) et les nœuds d’un même niveau sont espacés uniformément (`verticalSpacing`), les parents étant centrés par rapport à leurs enfants.
- Appliquer les flags (`showToast`, `setHidden`, `setFrameType`, `setIcon`) selon la définition.
- Retourner un objet `AdvancementTabPrototype` contenant :
  - `String rootIdentifier`;
  - `AdvancementRoot root`;
  - `List<AdvancementPrototype>` (refs vers les nœuds + meta comme `skillId`).

## 4. `AdvancementUiRegistry`
Package suggéré : `org.example.mmo.dev.advancementui`.

Fonctions :
- Charger toutes les définitions au démarrage (depuis JSON/YAML/DSL Java).
- Stocker `Map<String, AdvancementTabPrototype>`.
- Fournir `AdvancementTabPrototype get(String treeId)` et `Collection<String> listIds()`.
- Hot-reload :
  - watchers sur le répertoire de définition (optionnel) ou commande `/advui reload`.
  - Lors du reload, reconstruire les prototypes via `AdvancementGraphBuilder` et invalider les tabs actifs via `PlayerTabController` (voir §5).

## 5. `PlayerTabController`
Package suggéré : `org.example.mmo.mmo.player`.

Objectif : isoler la gestion d’un `AdvancementTab` par joueur.

Interface :
- `void openTree(Player player, String treeId)` :
  1. Clone le prototype (`AdvancementCloner` qui deep-copy `Advancement`/`AdvancementRoot`).
  2. Enregistre le tab dans un `ConcurrentHashMap<UUID, PlayerAdvancementTab>`.
  3. Appelle `AdvancementTab.addViewer(player)` et stocke la correspondance `skillId -> Advancement`.
- `void closeTrees(Player player)` : retire tous les tabs via `removeViewer`, supprime les entrées.
- `Optional<Advancement> getNode(Player player, String skillId)` pour que d’autres systèmes puissent mettre à jour `setAchieved`.
- `void rebuildAll(String treeId)` : utilisé par le reload pour fermer puis rouvrir les tabs d’un arbre donné.

Internalités :
- `PlayerAdvancementTab` contient `AdvancementTab tab`, `Map<String, Advancement> nodes`, `SkillTreeDefinition definition`.
- Le clonage peut passer par une fabrique dédiée (copier titres/desc/icônes/coords) car les instances `Advancement` sont mutables mais non clonables nativement.

## 6. `AdvancementUiFacade`
Interface publique dans `org.example.mmo.dev.advancementui`.

API prévue :
- `void showTree(Player player, String treeId)`
- `void hideTree(Player player, String treeId)`
- `void setNodeState(Player player, String skillId, SkillVisualState state)` (`ACHIEVED`, `AVAILABLE`, `LOCKED`, `HIDDEN`…) → applique `setAchieved`, `setHidden`, éventuellement change `icon`/`frame`.
- `void flashNode(Player player, String skillId, Duration duration)` → schedule une mise à jour (ex. passer en `FrameType.CHALLENGE` puis revert).
  - `Collection<String> listVisibleTrees(Player player)`
  - `boolean revealNode(Player player, String treeId, String nodeId)` : crée dynamiquement un nœud `secret`.

`SkillVisualState` encapsule la logique de mapping vers les attributs `Advancement` (toast, frame, hidden).

## 7. Outils Dev & Validation
- `AdvancementUiDevCommand` (dans `org.example.mmo.mmo.dev.commands`) :
  - `/advui open <tree>` : ouvre l’arbre pour le joueur courant.
  - `/advui close [tree]` : ferme un arbre ou tous.
  - `/advui reload [tree]` : relit les définitions + reconstruit les tabs.
  - `/advui inspect` : affiche dans le chat un dump (id, coords, état) obtenu via `PlayerTabController`.
- `AdvancementUiInspector` :
  - Méthode utilitaire qui retourne une table lisible (id, parent, x, y, frame, hidden).
  - Option de validation graphique (ex. export JSON vers un outil externe).
- `AdvancementUiDebugListener` :
  - Se branche sur `AdvancementTabEvent` pour logger ouverture/fermeture des tabs customs.

Tests :
- Tests unitaires sur `AdvancementGraphBuilder` (parent absent, coords hors bornes).
- Tests sur `PlayerTabController` : s’assurer qu’un changement sur un joueur n’impacte pas un autre (`setAchieved` reste local).

## 8. Intégration future
1. Ajouter la lecture des définitions (`docs/skill-trees/*.json`).
2. Initialiser `AdvancementUiRegistry` et `PlayerTabController` dans `GameLifecycle`.
3. Brancher `PlayerDataService` pour restaurer les états (`setAchieved(true)` avant `addViewer`).
4. Fournir un point d’entrée pour les modules de gameplay (combat/quests) via `AdvancementUiFacade`.
5. Documenter le workflow dans `docs/` (ce fichier) et maintenir une checklist lors de l’ajout d’un nouvel arbre.

# Guide de création d’un mob personnalisée

Ce document décrit le flux recommandé pour définir un nouveau mob, lui associer une IA personnalisée puis le faire apparaître dans une instance de jeu avec le système `org.example.mmo.npc.mob`.

## 1. Définir l’IA (behaviour)

Les comportements propres au gameplay se programment en implémentant l’interface `MobBehaviour`.  
Chaque instance reçoit les événements de cycle de vie (`onSpawn`, `onTick`, `onDamaged`, `onDeath`, etc.).

```java
package org.example.mmo.npc.mob.behaviour.impl;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.example.mmo.npc.mob.MobInstance;
import org.example.mmo.npc.mob.behaviour.MobBehaviour;

public final class BerserkerBehaviour implements MobBehaviour {

    private final LivingEntity entity;

    public BerserkerBehaviour(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void onAggro(MobInstance instance, Entity target) {
        entity.setSprinting(true); // Exemple : effet visuel
    }

    @Override
    public void onDamaged(MobInstance instance, net.minestom.server.entity.damage.Damage damage) {
        entity.setVelocity(entity.getVelocity().mul(1.05)); // boost simple
    }

    @Override
    public void onDeath(MobInstance instance, Entity killer) {
        entity.setCustomNameVisible(false);
    }
}
```

Pour raccorder ce comportement à un archétype, exposez un `MobBehaviourFactory` :

```java
MobBehaviourFactory berserker = (archetype, entity) -> new BerserkerBehaviour(entity);
```

## 2. Créer l’archétype

Un archétype (`MobArchetype`) encapsule l’entité Minestom, les stats, l’équipement, le loot et la liste de comportements.
Utilisez le builder statique fourni :

```java
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobEquipment;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.behaviour.MobBehaviourFactory;
import org.example.mmo.npc.mob.loot.MobLootEntry;
import org.example.mmo.npc.mob.loot.MobLootTable;
import org.example.mmo.item.datas.StatType;

MobBehaviourFactory berserker = (archetype, entity) -> new BerserkerBehaviour(entity);

MobEquipment equipment = MobEquipment.builder()
        .equip(net.minestom.server.entity.EquipmentSlot.MAIN_HAND, "iron_sword")
        .build();

var aiGroup = new EntityAIGroupBuilder()
        .addGoalSelector(new RandomStrollGoal(null, 20))            // null remplacé lors de l'appel build()
        .addGoalSelector(new MeleeAttackGoal(null, 1.6, 20, TimeUnit.SERVER_TICK))
        .addTargetSelector(new LastEntityDamagerTarget(null, 32))
        .addTargetSelector(new ClosestEntityTarget(null, 32, e -> e instanceof net.minestom.server.entity.Player))
        .build();

MobArchetype berserkerZombie = MobArchetype.builder("berserker_zombie", EntityType.ZOMBIE)
        .entityFactory(net.minestom.server.entity.type.monster.EntityZombie::new)
        .stat(StatType.HEALTH, 40)
        .equipment(equipment)
        .lootTable(new MobLootTable(List.of(
                new MobLootEntry("rotten_flesh", 0.75, 1, 3, MobLootCondition.ALWAYS_TRUE),
                new MobLootEntry("iron_ingot", 0.05, 1, 1, context -> context.killer().isPresent())
        )))
        .tag(MobTag.AGGRESSIVE)
        .baseAiGroup(aiGroup)
        .behaviourFactory(berserker)
        .build();

MobRegistry.register(berserkerZombie);
```

> 🔁 Note : si vous définissez un `EntityAIGroup`, remplacez les `null` par l’entité réelle avant d’appeler `build()`. Une option est d’instancier l’AI dans le `MobBehaviourFactory` ou via un helper qui injecte l’entité.

## 3. Faire apparaître l’entité dans le monde

Le `MobSpawnService` centralise l’instanciation et se récupère via `GameContext`.

```java
import net.minestom.server.coordinate.Pos;
import org.example.bootstrap.GameContext;
import org.example.mmo.npc.mob.MobSpawnService;
import org.example.mmo.npc.mob.MobRegistry;

MobSpawnService spawner = GameContext.get().mobSpawnService();
var archetype = MobRegistry.get("berserker_zombie");
if (archetype != null) {
    spawner.spawn(archetype, instance, new Pos(10, 65, -5));
}
```

Le service :

- instancie l’entité en utilisant `entityFactory`;
- applique les stats, l’équipement et la `customName`;
- étiquette l’entité avec `Tag.String("mmo:mob_archetype")` pour faciliter l’identification ;
- crée les comportements (`MobBehaviourFactory`) et enregistre l’instance auprès de `MobAiService`.

## 4. Drop de loot et extensions

- Les drops sont gérés par `MobLootTable` + `MobLootRoller`. Attachez un listener sur `EntityDeathEvent` pour appeler :

  ```java
  var mobInstance = GameContext.get().mobSpawnService().get(entityUuid);
  var context = MobLootContext.of(mobInstance, killer, looter, lastDamager);
  List<ItemStack> drops = MobLootRoller.generateLoot(mobInstance.archetype(), context, new Random());
  ```

- Pour la persistance ou des spawns scriptés, ajoutez un service dédié (ex : `MobSpawnScheduler`) qui orchestre `MobSpawnService`.

## 5. Tests et validations

- Compilez (`./gradlew build`), puis chargez une instance de dev et invoquez le mob (commande à venir).
- Vérifiez que la boss-bar et l’action-bar réagissent correctement lorsque vous combattez le mob.
- Inspectez la présence du tag `mmo:mob_archetype` (via un outil d’administration ou une commande de debug).

## 6. Bonnes pratiques

- Rassemblez les archetypes standards dans un module dédié (ex : `MobBootstrap`) pour un initialisation unique.
- Réutilisez des `MobBehaviourFactory` composables (ex : `new AggroBroadcastBehaviour(...)`) afin de mutualiser les patterns d’IA.
- Prévoyez des tests unitaires pour la génération de loot et les règles d’aggro complexes.

En suivant ces étapes, vous pouvez créer rapidement de nouveaux ennemis avec un comportement cohérent, tout en conservant une architecture propre et extensible. Bon développement !

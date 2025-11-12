# Mob Creation & Spawning Zones Guide

This note explains how to introduce a new mob, attach an AI behaviour, and populate it inside the world using the spawning-zone system.

## 1. Create behaviours

Gameplay logic lives inside implementations of `MobBehaviour`. Extend `MobBehaviourAdapter` when you only need a few hooks:

```java
package org.example.mmo.npc.mob.behaviour.behaviours;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.example.mmo.npc.mob.MobBehaviourAdapter;
import org.example.mmo.npc.mob.MobInstance;

public final class BerserkShoutBehaviour extends MobBehaviourAdapter {

    @Override
    public void onAggro(MobInstance instance, Entity target) {
        if (target instanceof Player player) {
            player.sendMessage("Le berserker fonce sur vous !");
        }
    }
}
```

Factory assignment happens through `MobBehaviourFactory`:  
`MobBehaviourFactory berserkShout = (archetype, entity) -> new BerserkShoutBehaviour();`

## 2. Define the mob archetype

`MobArchetype` captures ID, quest-friendly name, entity type, stats, equipment, loot, AI and behaviours.  
You can refer to the demo archetypes in `org.example.mmo.npc.mob.archetype.archetypes` (`ForestWolfMob`, `BanditSkirmisherMob`, `BanditArcherMob`) for real examples.

```java
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.MobAiFactories;
import org.example.mmo.npc.mob.behaviour.behaviours.BerserkShoutBehaviour;
import org.example.mmo.npc.mob.loot.MobLootTable;

MobArchetype berserker = MobArchetype.builder("berserker_raider", "Pillard berserker", EntityType.ZOMBIE)
        .entityFactory(() -> new EntityCreature(EntityType.ZOMBIE))
        .aiFactory(MobAiFactories.meleeCharger(1.3, 25))
        .stat(StatType.HEALTH, 40)
        .tag(MobTag.AGGRESSIVE)
        .displayName(Component.text("Pillard berserker"))
        .behaviourFactory((archetype, entity) -> new BerserkShoutBehaviour())
        .lootTable(MobLootTable.EMPTY)
        .build();

MobRegistry.

register(berserker);
```

Key points:
- `id` is the code reference (use it for quests/objectives); `name` is the human-readable label.
- `entityFactory` should return a `LivingEntity` (often `new EntityCreature(EntityType.XXX)`).
- Use `MobAiFactories` for common pathing/attack combos or provide your custom `MobAiFactory`.
- Behaviours are optional; provide as many factories as needed.

The demonstration mobs are registered in `MobBootstrap.init()`.

## 3. Spawn on demand

To spawn an archetype manually:

```java
import net.minestom.server.coordinate.Pos;
import org.example.bootstrap.GameContext;
import org.example.mmo.npc.mob.MobRegistry;

var archetype = MobRegistry.get("berserker_raider");
if (archetype != null) {
    GameContext.get().mobSpawnService().spawn(archetype, instance, new Pos(12, 65, -8));
}
```

`MobSpawnService` applies stats, equipment, AI, behaviours and adds the tracking tag `mmo:mob_archetype`.

## 4. Configure spawning zones

Spawning zones keep hunting areas populated. Define a zone and register it with `MobSpawningZoneService`.  
See `org.example.mmo.npc.mob.zone.demo` (`WolfGroveZone`, `BanditCampZone`) for ready-made hunting grounds.

```java
import java.time.Duration;
import java.util.List;
import net.minestom.server.coordinate.Pos;
import org.example.bootstrap.GameContext;
import org.example.mmo.npc.mob.zone.MobSpawningZone;

MobSpawningZone wolves = MobSpawningZone.create(
        "wolves_grove",
        "Clairière des loups",
        instance,
        new Pos(48, 64, -32),
        12.0,
        List.of("forest_wolf", "bandit_skirmisher"),
        List.of(4, 2),
        Duration.ofSeconds(30)
);

GameContext.get().mobSpawningZoneService().registerZone(wolves);
```

- `mobIds` and `maxAlive` lists must have the same length.
- Each slot keeps up to the specified number of mobs alive at any time; when one dies, a respawn token is queued after the configured delay.
- Zones automatically spawn their initial population and re-populate as long as the zone stays registered.

To remove a zone: `GameContext.get().mobSpawningZoneService().unregisterZone("wolves_grove");`

## 5. Loot generation

Attach loot entries to the archetype and use `MobLootRoller` (usually inside an `EntityDeathEvent` listener) to materialise drops:

```java
var mobInstance = GameContext.get().mobSpawnService().get(entity.getUuid());
mobInstance.ifPresent(instance -> {
    var context = MobLootContext.of(instance, killer, killer instanceof Player ? (Player) killer : null, null);
    var drops = MobLootRoller.generateLoot(instance.archetype(), context, RANDOM);
    drops.forEach(stack -> instance.instance().dropItem(stack, entity.getPosition()));
});
```

## 6. Validation checklist

- Run `./gradlew build` once write access is available.
- Spawn the new archetype via command or zone, verify:
  * custom name matches expectations,
  * behaviours trigger (logs/messages/effects),
  * AI chases intended targets.
- Kill the mob and ensure the spawning zone replenishes the population after the respawn delay.
- Check the entity carries the `mmo:mob_archetype` tag with the correct ID.

With these pieces you can script quêtes, hunting grounds, and reactive mobs while keeping the implementation modular and testable.






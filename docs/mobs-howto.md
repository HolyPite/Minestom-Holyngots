# Creating Mobs, AI, and Spawn Zones

This walkthrough explains how to introduce a new mob archetype, wire custom AI/behaviours, and populate the creature in every game instance via spawning zones.

---

## 1. Know the building blocks

| Responsibility | Key types | Location |
| --- | --- | --- |
| Archetype definition & registry | `MobArchetype`, `MobRegistry`, `MobBootstrap` | `org.example.mmo.npc.mob.archetype` |
| Behaviour hooks (buffs, enrages, scripts) | `MobBehaviour`, `MobBehaviourAdapter` | `org.example.mmo.npc.mob.behaviour` |
| AI goal plumbing | `MobAiFactory`, `MobAiFactories`, `MobAiService` | `org.example.mmo.npc.mob.ai` |
| Spawning & cleanup | `MobSpawnService`, `MobInstance` | `org.example.mmo.npc.mob` |
| Hunting grounds | `MobSpawningZone`, `MobSpawningZoneService`, `MobZoneDefinitions` | `org.example.mmo.npc.mob.zone` |

At runtime, `MobBootstrap.init()` registers every archetype, `MobZoneBootstrap.init()` iterates over all game instances to register zones, and `MobSpawningZoneService` keeps populations topped up.

---

## 2. Create AI goals (optional but recommended)

If a built-in preset from `MobAiFactories` fits (e.g. `meleeCharger`, `archer`, `passiveSentry`), reuse it. Otherwise, supply your own `MobAiFactory` implementation:

```java
package org.example.mmo.npc.mob.ai.custom;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.npc.mob.MobAiFactory;

public final class GuardianAiFactory {

    private GuardianAiFactory() {
    }

    public static MobAiFactory create() {
        return entity -> {
            if (!(entity instanceof EntityCreature creature)) {
                return null;
            }
            return new EntityAIGroupBuilder()
                    .addGoalSelector(new RandomStrollGoal(creature, 40))
                    .addGoalSelector(new MeleeAttackGoal(creature, 1.2, 30, TimeUnit.SERVER_TICK))
                    .addTargetSelector(new ClosestEntityTarget(creature, 32f, target -> target.getEntityType().isPlayer()))
                    .build();
        };
    }
}
```

Attach projectile logic with `MobProjectileUtils.shootArrow(...)` if you need custom ranged attacks.

---

## 3. Implement optional behaviours

Behaviours complement AI by reacting to lifecycle events such as spawn, aggro, damage, and death. Extend `MobBehaviourAdapter` when only a subset of hooks is needed.

```java
package org.example.mmo.npc.mob.behaviour.behaviours;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import org.example.mmo.combat.util.CombatFeedback;
import org.example.mmo.npc.mob.MobBehaviourAdapter;
import org.example.mmo.npc.mob.MobInstance;

public final class RallyingCryBehaviour extends MobBehaviourAdapter {

    @Override
    public void onAggro(MobInstance instance, Entity target) {
        CombatFeedback.sendBossBar(instance, Component.text("Le garde appelle du renfort !"));
    }

    @Override
    public void onDeath(MobInstance instance, Entity killer) {
        // Trigger quest hooks or spawn allies here
    }
}
```

Register behaviours on the archetype (see next section) by providing a `MobBehaviourFactory`.

---

## 4. Define the mob archetype

Create a dedicated class under `org.example.mmo.npc.mob.archetype.<category>` (e.g. `guardian/CityGuardianMob.java`). Every mob needs a unique `id`, a display `name`, and should register itself via `MobRegistry`.

```java
package org.example.mmo.npc.mob.archetype.guardian;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.custom.GuardianAiFactory;
import org.example.mmo.npc.mob.behaviour.behaviours.RallyingCryBehaviour;
import org.example.mmo.npc.mob.loot.MobLootTable;

public final class CityGuardianMob {

    public static final String ID = "city_guardian";
    public static final String NAME = "Garde de la cité";

    private CityGuardianMob() {
    }

    public static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.IRON_GOLEM)
                .entityFactory(() -> new EntityCreature(EntityType.IRON_GOLEM))
                .displayName(Component.text(NAME))
                .aiFactory(GuardianAiFactory.create())
                .stat(StatType.HEALTH, 120)
                .tag(MobTag.AGGRESSIVE)
                .behaviourFactory((archetype, entity) -> new RallyingCryBehaviour())
                .lootTable(MobLootTable.EMPTY) // swap for a real table
                .build();
    }

    public static void register() {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(create());
        }
    }
}
```

Finally, call `CityGuardianMob.register()` from `MobBootstrap.init()` to make the archetype available at startup.

---

## 5. Add loot or equipment (optional)

- Equip gear with `MobArchetype.builder(...).equipment(slot, "item_id")`.
- Define loot tables under `org.example.mmo.npc.mob.loot` using `MobLootEntry` and `MobLootTable`.
- Hooks fire in `MobSpawnService` and `MobSpawningZoneService` when the mob dies, so loot rolls will execute automatically once configured.

---

## 6. Describe a spawning zone

Zones are data-first. Add a new entry to `MobZoneDefinitions.ZONES` (or split into your own provider list) using the helper record `MobZoneDefinition`.

```java
package org.example.mmo.npc.mob.zone.zones;

import java.time.Duration;
import java.util.List;

import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.mob.zone.MobZoneDefinition;

public final class MobZoneDefinitions {

    public static final List<MobZoneDefinition> ZONES = List.of(
            // existing entries...
            new MobZoneDefinition(
                    "city_gate_patrol",
                    "Patrouille de la porte",
                    new Pos(128.5, 40.0, -12.0),
                    10.0,
                    List.of("city_guardian"),
                    List.of(3),
                    Duration.ofSeconds(45)
            )
    );
}
```

> **Important:** The `mobIds` list must reference registered archetype IDs, and `maxAlive` must have the same size. Zones are duplicated across every instance whose name starts with `game` by `MobZoneRegistrations.registerForAllInstances(...)`.

---

## 7. Link everything together

1. Ensure `MobBootstrap.init()` invokes your new `register()` method.
2. Update `MobZoneDefinitions.ZONES` (or your equivalent module) with the new zone.
3. Keep the bootstrap wiring untouched: `MobZoneBootstrap.init(...)` already iterates zones for each game instance at startup.

With these steps, the spawn service will instantiate the archetype in every eligible instance, maintain population counts, and handle respawn timers.

---

## 8. Manual testing checklist

- Run `./gradlew build` to confirm the project compiles.
- Launch the server (`java -jar build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar`).
- Teleport near the zone centre; verify mobs spawn with correct nameplate (`Nom du mob  /  PV`), bossbar, and behaviours.
- Attack and defeat the mob:
  - Confirm AI targets players only.
  - Check loot drops and respawn delay.
  - Ensure floating damage indicators/action bar feedback match expectations.
- For ranged units, confirm projectiles are spawned by `MobProjectileUtils` and travel correctly.

---

## 9. Quick reference: add a new mob in three steps

1. **AI & behaviour** – create optional `MobBehaviour` and `MobAiFactory` classes under `npc.mob.behaviour` / `npc.mob.ai`.
2. **Archetype** – add a class (e.g. `MyMob.java`) under `npc.mob.archetype.<feature>` returning a configured `MobArchetype` and register it in `MobBootstrap`.
3. **Spawn zone** – append a `MobZoneDefinition` entry so `MobZoneBootstrap` deploys it across all game instances.

Repeat the process for additional mobs, behaviours, or hunting grounds. Keep archetypes, behaviours, and zones in their respective packages to maintain the clean separation introduced by the reorganisation.

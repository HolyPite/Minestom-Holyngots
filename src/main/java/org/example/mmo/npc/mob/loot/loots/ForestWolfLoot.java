package org.example.mmo.npc.mob.loot.loots;

import java.util.List;
import org.example.mmo.npc.mob.loot.MobLootCondition;
import org.example.mmo.npc.mob.loot.MobLootEntry;
import org.example.mmo.npc.mob.loot.MobLootTable;

/**
 * Loot table presets for the forest wolf archetype.
 */
public final class ForestWolfLoot {

    public static final MobLootTable TABLE = new MobLootTable(List.of(
            new MobLootEntry("wolf_pelt", 0.85, 1, 2, MobLootCondition.ALWAYS_TRUE),
            new MobLootEntry("wolf_fang", 0.30, 1, 1, MobLootCondition.ALWAYS_TRUE),
            MobLootEntry.of("bone", 0.40)
    ));

    private ForestWolfLoot() {
    }
}

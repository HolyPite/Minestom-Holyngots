package org.example.mmo.mob.loot.loots;

import java.util.List;
import org.example.mmo.mob.loot.MobLootCondition;
import org.example.mmo.mob.loot.MobLootEntry;
import org.example.mmo.mob.loot.MobLootTable;

/**
 * Loot table presets for the bandit archer archetype.
 */
public final class BanditArcherLoot {

    public static final MobLootTable TABLE = new MobLootTable(List.of(
            new MobLootEntry("bandit_coin", 0.55, 1, 2, MobLootCondition.ALWAYS_TRUE),
            new MobLootEntry("bandit_orders", 0.18, 1, 1, MobLootCondition.ALWAYS_TRUE),
            MobLootEntry.of("excalibur", 0.02)
    ));

    private BanditArcherLoot() {
    }
}

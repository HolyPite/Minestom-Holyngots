package org.example.mmo.mob.loot.loots;

import java.util.List;
import org.example.mmo.mob.loot.MobLootCondition;
import org.example.mmo.mob.loot.MobLootEntry;
import org.example.mmo.mob.loot.MobLootTable;

/**
 * Loot table presets for the bandit skirmisher archetype.
 */
public final class BanditSkirmisherLoot {

    public static final MobLootTable TABLE = new MobLootTable(List.of(
            new MobLootEntry("bandit_coin", 0.65, 1, 3, MobLootCondition.ALWAYS_TRUE),
            new MobLootEntry("bandit_orders", 0.12, 1, 1, MobLootCondition.ALWAYS_TRUE),
            MobLootEntry.of("polstron", 0.05)
    ));

    private BanditSkirmisherLoot() {
    }
}

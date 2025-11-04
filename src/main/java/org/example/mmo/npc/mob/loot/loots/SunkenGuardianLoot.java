package org.example.mmo.npc.mob.loot.loots;

import java.util.List;
import org.example.mmo.npc.mob.loot.MobLootCondition;
import org.example.mmo.npc.mob.loot.MobLootEntry;
import org.example.mmo.npc.mob.loot.MobLootTable;

/**
 * Loot setup for the sunken guardian archetype.
 */
public final class SunkenGuardianLoot {

    public static final MobLootTable TABLE = new MobLootTable(List.of(
            new MobLootEntry("sunken_guardian_halberd", 0.04, 1, 1, MobLootCondition.ALWAYS_TRUE),
            new MobLootEntry("sunken_guardian_plate", 0.10, 1, 1, MobLootCondition.ALWAYS_TRUE),
            MobLootEntry.of("polstron", 0.25)
    ));

    private SunkenGuardianLoot() {
    }
}

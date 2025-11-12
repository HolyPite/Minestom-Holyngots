package org.example.mmo.mob.loot.loots;

import java.util.List;
import org.example.mmo.mob.loot.MobQuestLootEntry;
import org.example.mmo.mob.loot.MobQuestLootTable;
import org.example.mmo.mob.loot.QuestLootConditions;

public final class SunkenGuardianQuestLoot {

    public static final MobQuestLootTable TABLE = new MobQuestLootTable(List.of(
            new MobQuestLootEntry(
                    "sunken_guardian_ember",
                    0.75,
                    1,
                    1,
                    QuestLootConditions.questStep("fire_purge", 0)
            )
    ));

    private SunkenGuardianQuestLoot() {
    }
}

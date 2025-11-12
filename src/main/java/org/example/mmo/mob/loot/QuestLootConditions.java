package org.example.mmo.mob.loot;

import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.structure.QuestProgress;

/**
 * Convenience factory for quest-related loot conditions.
 */
public final class QuestLootConditions {

    private QuestLootConditions() {
    }

    public static QuestLootCondition questStep(String questId, int stepIndex) {
        return (player, data) -> hasQuestAtStep(data, questId, stepIndex);
    }

    private static boolean hasQuestAtStep(PlayerData data, String questId, int stepIndex) {
        if (data == null || data.quests == null) {
            return false;
        }
        for (QuestProgress progress : data.quests) {
            if (progress != null && questId.equals(progress.questId) && progress.stepIndex == stepIndex) {
                return true;
            }
        }
        return false;
    }
}

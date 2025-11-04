package org.example.mmo.mob.loot;

/**
 * Represents a conditional quest drop entry from a mob archetype.
 */
public record MobQuestLootEntry(String itemId,
                                double chance,
                                int minAmount,
                                int maxAmount,
                                QuestLootCondition condition) {

    public MobQuestLootEntry {
        if (chance < 0d || chance > 1d) {
            throw new IllegalArgumentException("Chance must be between 0 and 1");
        }
        if (minAmount < 0 || maxAmount < minAmount) {
            throw new IllegalArgumentException("Invalid amount bounds");
        }
        if (condition == null) {
            condition = QuestLootCondition.ALWAYS_TRUE;
        }
    }

    public static MobQuestLootEntry of(String itemId,
                                       double chance,
                                       QuestLootCondition condition) {
        return new MobQuestLootEntry(itemId, chance, 1, 1, condition);
    }
}

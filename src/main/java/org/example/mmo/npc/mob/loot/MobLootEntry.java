package org.example.mmo.npc.mob.loot;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a single loot roll entry for an archetype.
 *
 * @param itemId identifier from the item registry
 * @param chance drop probability (0-1)
 * @param minAmount minimum quantity if the roll succeeds
 * @param maxAmount maximum quantity if the roll succeeds
 * @param condition optional condition to validate before rolling
 */
public record MobLootEntry(@NotNull String itemId,
                           double chance,
                           int minAmount,
                           int maxAmount,
                           @NotNull MobLootCondition condition) {

    public MobLootEntry {
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Chance must be between 0 and 1");
        }
        if (minAmount < 0 || maxAmount < minAmount) {
            throw new IllegalArgumentException("Invalid amount bounds");
        }
    }

    public static MobLootEntry of(String itemId, double chance) {
        return new MobLootEntry(itemId, chance, 1, 1, MobLootCondition.ALWAYS_TRUE);
    }
}

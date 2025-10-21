package org.example.mmo.quest;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

/**
 * Interface representing a reward for completing a quest step.
 * This allows for different types of rewards (items, experience, money, etc.).
 */
public interface IQuestReward {

    /**
     * Applies the reward to the player.
     * @param player The player to receive the reward.
     */
    void apply(Player player);

    /**
     * Gets the description of the reward for the quest journal.
     * @return The reward's description text.
     */
    Component getDescription();
}

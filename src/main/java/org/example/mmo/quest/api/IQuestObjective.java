package org.example.mmo.quest.api;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;

/**
 * Interface representing a single objective within a quest step.
 * This allows for modular and varied objectives (fetch, kill, locate, etc.).
 */
public interface IQuestObjective {

    /**
     * Gets the description of the objective for the quest journal.
     * @return The objective's description text.
     */
    Component getDescription();

    /**
     * Checks if the objective has been completed by the player.
     * @param player The player to check.
     * @param data The player's data, containing quest progress.
     * @return true if the objective is completed, false otherwise.
     */
    boolean isCompleted(Player player, PlayerData data);

    /**
     * Called when the quest step containing this objective begins.
     * Use this to initialize state or register temporary event listeners.
     * @param player The player starting the objective.
     * @param data The player's data.
     */
    void onStart(Player player, PlayerData data);

    /**
     * Called when the quest step is completed or abandoned.
     * Use this to clean up, for example by unregistering event listeners.
     * @param player The player completing the objective.
     * @param data The player's data.
     */
    void onComplete(Player player, PlayerData data);

    /**
     * Called when a quest step is failed and needs to be reset.
     * Use this to clear any progress counters associated with this objective.
     * @param player The player who failed.
     * @param data The player's data.
     */
    void onReset(Player player, PlayerData data);
}

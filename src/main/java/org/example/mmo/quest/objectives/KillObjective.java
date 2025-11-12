package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.api.IQuestObjective;

/**
 * An objective that requires the player to kill a certain number of a specific mob archetype.
 */
public class KillObjective implements IQuestObjective {

    private final String mobId;
    private final int count;
    private final String progressId;
    private final Component description;

    public KillObjective(String mobId, int count, String progressId, Component description) {
        this.mobId = mobId;
        this.count = count;
        this.progressId = progressId; // Unique ID to track progress, e.g., "kill_zombies_for_try_quest"
        this.description = description;
    }

    public String getMobId() {
        return mobId;
    }

    public String getProgressId() {
        return progressId;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        return data.getQuestCounter(progressId) >= count;
    }

    @Override
    public void onStart(Player player, PlayerData data) {
        // Initialize the counter if it doesn't exist
        data.setQuestCounter(progressId, 0);
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        // The counter can be left as is, or reset if you want the objective to be repeatable in the future
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        // Reset the kill count for this objective
        data.setQuestCounter(progressId, 0);
    }
}

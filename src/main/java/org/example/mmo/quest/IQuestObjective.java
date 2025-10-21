package org.example.mmo.quest;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;

public interface IQuestObjective {

    Component getDescription();

    boolean isCompleted(Player player, PlayerData data);

    void onStart(Player player, PlayerData data);

    void onComplete(Player player, PlayerData data);

    /**
     * Called when a quest step is failed and needs to be reset.
     * Use this to clear any progress counters associated with this objective.
     * @param player The player who failed.
     * @param data The player's data.
     */
    void onReset(Player player, PlayerData data);
}

package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.utils.TKit;

/**
 * An objective that requires the player to possess a certain quantity of a specific item.
 */
public class FetchObjective implements IQuestObjective {

    private final GameItem itemToFetch;
    private final int requiredAmount;
    private final Component description;

    public FetchObjective(GameItem itemToFetch, int requiredAmount, Component description) {
        this.itemToFetch = itemToFetch;
        this.requiredAmount = requiredAmount;
        this.description = description;
    }

    public GameItem getItemToFetch() {
        return itemToFetch;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        return TKit.countItems(player, itemToFetch.toItemStack()) >= requiredAmount;
    }

    @Override
    public void onStart(Player player, PlayerData data) {
        // No action needed on start
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        // FIX: Do nothing here. Items will be removed when the STEP is completed.
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        // No state to reset for this objective type
    }
}

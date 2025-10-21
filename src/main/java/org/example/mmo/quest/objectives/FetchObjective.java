package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.utils.TKit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An objective that requires the player to possess specific items.
 * The logic is now delegated to TKit for inventory management.
 */
public class FetchObjective implements IQuestObjective {

    private final List<GameItem> itemsToFetch;
    private final Component description;

    public FetchObjective(List<GameItem> itemsToFetch, Component description) {
        this.itemsToFetch = itemsToFetch;
        this.description = description;
    }

    private List<ItemStack> getObjectiveAsItemStackList() {
        return itemsToFetch.stream()
                .map(GameItem::toItemStack)
                .collect(Collectors.toList());
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        if (itemsToFetch == null || itemsToFetch.isEmpty()) {
            return true;
        }
        // Check each item individually using the TKit.hasItems method.
        for (GameItem item : itemsToFetch) {
            // For simplicity, we assume the objective requires 1 of each item.
            if (!TKit.hasItems(player, item.toItemStack(), 1)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStart(Player player, PlayerData data) {
        // No action needed on start.
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        if (itemsToFetch == null || itemsToFetch.isEmpty()) {
            return;
        }
        // Use the transactional method from TKit to remove all items at once.
        TKit.removeItemsList(player, getObjectiveAsItemStackList());
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        // No state to reset for this objective type.
    }
}

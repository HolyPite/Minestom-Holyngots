package org.example.mmo.quest.rewards;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.mmo.item.GameItem;
import org.example.mmo.quest.api.IQuestReward;

import java.util.List;

/**
 * A reward that gives one or more items to the player.
 */
public class ItemReward implements IQuestReward {

    private final List<GameItem> items;

    public ItemReward(List<GameItem> items) {
        this.items = items;
    }

    @Override
    public void apply(Player player) {
        if (items == null) return;
        for (GameItem item : items) {
            player.getInventory().addItemStack(item.toItemStack());
        }
    }

    @Override
    public Component getDescription() {
        // A more sophisticated description could be generated here, listing the items.
        return Component.text("Item Reward");
    }
}

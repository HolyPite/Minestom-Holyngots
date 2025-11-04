package org.example.mmo.item;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.example.utils.TKit;

/**
 * Utility helpers to deliver GameItems to players while gracefully handling full inventories.
 */
public final class ItemDelivery {

    private ItemDelivery() {
    }

    public static void giveOrDrop(Player player, ItemStack stack, Instance instance, Pos dropPosition) {
        if (player == null || stack == null || stack.isAir()) {
            return;
        }
        Instance targetInstance = instance != null ? instance : player.getInstance();
        Pos targetPos = dropPosition != null ? dropPosition : player.getPosition();
        boolean added = player.getInventory().addItemStack(stack);
        if (!added && targetInstance != null) {
            TKit.drop(targetInstance, stack, targetPos);
        }
    }
}

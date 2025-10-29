package org.example.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class ToastManager {
    private ToastManager() {}

    public static void showToast(Player player, String title, String description, Material icon, FrameType frame) {
        if (player == null) return;

        Notification toast = new Notification(
                Component.text(title),
                frame,
                ItemStack.of(icon)
        );
        player.sendNotification(toast);

        if (description != null && !description.isEmpty()) {
            player.sendActionBar(Component.text(description));
        }
    }

    public static void showToast(Player player, Component title, FrameType frame, ItemStack icon, Component actionBar) {
        if (player == null) return;
        player.sendNotification(new Notification(title, frame, icon));
        if (actionBar != null) player.sendActionBar(actionBar);
    }
}

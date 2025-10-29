package org.example.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class ToastManager {
    private ToastManager() {}

    /**
     * Shows a toast notification to the player.
     * The title of the toast is determined by the FrameType.
     * @param player The player to show the toast to.
     * @param description The component to display as the main content (the second line) of the toast.
     * @param icon The material to use as the icon.
     * @param frame The frame type, which determines the title and the border style.
     */
    public static void showToast(Player player, Component description, Material icon, FrameType frame) {
        if (player == null) return;

        Notification toast = new Notification(
                description,      // This is the content (second line)
                frame,            // This determines the title (first line)
                ItemStack.of(icon)
        );
        player.sendNotification(toast);
    }
}

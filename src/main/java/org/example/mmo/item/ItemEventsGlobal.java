package org.example.mmo.item;

import java.util.Objects;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import org.example.bootstrap.GameContext;
import org.example.mmo.item.datas.Stats;
import org.example.mmo.mob.loot.MobLootBundles;

public class ItemEventsGlobal {
    public static void init(EventNode<Event> events){

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode", InventoryEvent.class).getFirst();

        ItemProjectileListener.init(playerNode);

        // The InventoryItemChangeEvent listener was removed as it could fire too early
        // during the player configuration phase, causing crashes.
        // The initial stat refresh is now handled in the PlayerSpawnEvent in Main.java.

        // This listener is safe as it only fires when the player is in-game.
        playerNode.addListener(PlayerChangeHeldSlotEvent.class, e -> {
            Player p = e.getEntity();
            p.scheduler().buildTask(() -> {
                if (Objects.equals(GameContext.get().instances().nameOfGroup(GameContext.get().instances().groupFor(p.getInstance())), "games"))   {
                    Stats.refresh(p);
                }
            }).delay(TaskSchedule.tick(1)).schedule();
        });

        playerNode.addListener(PlayerUseItemEvent.class, event -> {
            ItemStack stack = event.getItemStack();
            if (!MobLootBundles.isBundle(stack)) {
                return;
            }
            if (MobLootBundles.openBundle(event.getPlayer(), stack)) {
                consumeFromHand(event.getPlayer(), event.getHand(), stack);
            }
            event.setCancelled(true);
        });

        inventoryNode.addListener(InventoryPreClickEvent.class, event -> {
            ItemStack clicked = event.getClickedItem();
            if (clicked == null || clicked.isAir() || !MobLootBundles.isBundle(clicked)) {
                return;
            }
            Click click = event.getClick();
            if (!(click instanceof Click.Right)) {
                return;
            }
            if (!MobLootBundles.openBundle(event.getPlayer(), clicked)) {
                return;
            }
            event.setCancelled(true);
            ItemStack remaining = decrement(clicked);
            AbstractInventory inventory = event.getInventory();
            int slot = event.getSlot();
            if (remaining.isAir()) {
                inventory.setItemStack(slot, ItemStack.AIR);
            } else {
                inventory.setItemStack(slot, remaining);
            }
        });
    }

    private static void consumeFromHand(Player player, PlayerHand hand, ItemStack stack) {
        ItemStack remaining = decrement(stack);
        if (hand == PlayerHand.MAIN) {
            player.setItemInMainHand(remaining);
        } else {
            player.setItemInOffHand(remaining);
        }
    }

    private static ItemStack decrement(ItemStack stack) {
        int amount = stack.amount();
        if (amount <= 1) {
            return ItemStack.AIR;
        }
        return stack.withAmount(amount - 1);
    }
}

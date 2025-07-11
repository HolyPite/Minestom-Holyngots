// org.example.combats.CustomItemEvents
package org.example.items;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.*;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class ItemEventsCustom {

    /* ------------------------------------------------------------------ */
    /* 1)  public API – registration                                       */
    /* ------------------------------------------------------------------ */
    public interface Behaviour {

        //Inventory clic
        default void onInventoryClic(Player p, ItemStack stack, InventoryPreClickEvent e) {}
        default void onInventoryChange(ItemStack stack, InventoryItemChangeEvent e) {}

        //Left-Clic
        default void onLeftClickAir(Player p, ItemStack stack) {}
        default void onLeftClickEntity(Player p, Entity target, ItemStack stack) {}

        //Right-Clic
        default void onRightClickAir(Player p, ItemStack stack) {}
        default void onRightClickBlock(Player p, Point blockPos, ItemStack stack) {}

        //Every tick
        default void onHeldTick(Player p, ItemStack held) {}

    }

    private static final Map<String, Behaviour> BEHAVIOURS = new HashMap<>();

    /** to be called by the custom item class (once, at bootstrap) */
    public static void register(GameItem item, Behaviour behaviour) {
        BEHAVIOURS.put(item.id, behaviour);
    }

    /* ------------------------------------------------------------------ */
    /* 2)  single wiring on the global event bus                           */
    /* ------------------------------------------------------------------ */
    public static void init(GlobalEventHandler events) {

        /* Inventory clic ------------------------------------------------ */
        events.addListener(InventoryPreClickEvent.class, e -> {
            Behaviour b = behaviour(e.getClickedItem());
            if (b != null) {
                b.onInventoryClic(e.getPlayer(), e.getClickedItem(), e);
            }
        });

        /* Inventory Change ------------------------------------------------*/
        events.addListener(InventoryItemChangeEvent.class, e -> {
            Behaviour b1 = behaviour(e.getPreviousItem());
            Behaviour b2 = behaviour(e.getNewItem());
            if (b1 != null) {
                b1.onInventoryChange(e.getPreviousItem(),e);
            }
            if (b2 != null) {
                b2.onInventoryChange(e.getNewItem(),e);
            }
        });

        /* right-click air ------------------------------------------------ */
        events.addListener(PlayerUseItemEvent.class, e -> {
            Behaviour b = behaviour(e.getItemStack());
            if (b != null) {
                b.onRightClickAir(e.getPlayer(), e.getItemStack());
                e.setCancelled(true);
            }
        });

        /* right-click block --------------------------------------------- */
        events.addListener(PlayerUseItemOnBlockEvent.class, e -> {
            ItemStack inHand = e.getItemStack();
            Behaviour b = behaviour(inHand);
            if (b != null) {
                b.onRightClickBlock(e.getPlayer(), e.getPosition(), inHand);
            }
        });

        events.addListener(PlayerBlockPlaceEvent.class, e -> {
            ItemStack inHand = e.getEntity().getItemInMainHand();
            Behaviour b = behaviour(inHand);
            if (b != null) {
                b.onRightClickBlock(e.getPlayer(), e.getBlockPosition(), inHand);
                e.setCancelled(true);
            }
        });

        /* left-click entity --------------------------------------------- */
        events.addListener(EntityAttackEvent.class, e -> {
            if (!(e.getEntity() instanceof Player p)) return;
            ItemStack inHand = p.getItemInMainHand();
            Behaviour b = behaviour(inHand);
            if (b != null)
                b.onLeftClickEntity(p, e.getTarget(), inHand);     // no cancel → damage still applies
        });

        /* left-click air (arm-swing with no victim) --------------------- */
        events.addListener(PlayerHandAnimationEvent.class, e -> {
            Player p = e.getPlayer();
            PlayerHand h = e.getHand();
            Behaviour b = behaviour(p.getItemInHand(h));
            if (b != null)
                b.onLeftClickAir(p, p.getItemInHand(h));
        });

        /* tick while held ----------------------------------------------- */
        events.addListener(EntityTickEvent.class, e -> {
            if (!(e.getEntity() instanceof Player p)) return;
            ItemStack inHand = p.getItemInMainHand();
            Behaviour b = behaviour(inHand);
            if (b != null)
                b.onHeldTick(p, inHand);
        });
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */
    private static Behaviour behaviour(ItemStack stack) {
        GameItem gi = ItemUtils.resolve(stack);
        return gi == null ? null : BEHAVIOURS.get(gi.id);
    }

    private ItemEventsCustom() {}
}

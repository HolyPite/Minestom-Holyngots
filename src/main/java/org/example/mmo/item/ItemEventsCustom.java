// org.example.mmo.combats.CustomItemEvents
package org.example.mmo.item;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.skill.SkillTrigger;
import org.example.mmo.item.skill.SkillTriggerData;

public final class ItemEventsCustom {

    /* ------------------------------------------------------------------ */
    /* 2)  single wiring on the global event bus                           */
    /* ------------------------------------------------------------------ */
    public static void init(EventNode<Event> events) {

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();
        EventNode<EntityEvent> entityNode = events.findChildren("entityNode",EntityEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode",InventoryEvent.class).getFirst();

        /* Inventory clic ------------------------------------------------ */
        inventoryNode.addListener(InventoryPreClickEvent.class, e -> {
            ItemStack clicked = e.getClickedItem();
            triggerSkills(e.getPlayer(), clicked, SkillTrigger.INVENTORY_CLICK, new SkillTriggerData.InventoryClickData(e));
        });

        /* Inventory Change ------------------------------------------------*/
        inventoryNode.addListener(InventoryItemChangeEvent.class, e -> {
            var data = new SkillTriggerData.InventoryChangeData(e, e.getPreviousItem(), e.getNewItem());

            AbstractInventory inv = e.getInventory();

            if (inv.getViewers().isEmpty()) return;
            Player player = inv.getViewers().iterator().next();

            triggerSkills(player, e.getPreviousItem(), SkillTrigger.INVENTORY_CHANGE, data);
            triggerSkills(player, e.getNewItem(), SkillTrigger.INVENTORY_CHANGE, data);
        });

        /* right-click air ------------------------------------------------ */
        playerNode.addListener(PlayerUseItemEvent.class, e -> {
            boolean activated = triggerSkills(e.getPlayer(), e.getItemStack(), SkillTrigger.RIGHT_CLICK_AIR, new SkillTriggerData.SimpleData());
            if (activated) {
                e.setCancelled(true);
            }
        });

        /* right-click block --------------------------------------------- */
        playerNode.addListener(PlayerUseItemOnBlockEvent.class, e -> {
            ItemStack inHand = e.getItemStack();
            triggerSkills(e.getPlayer(), inHand, SkillTrigger.RIGHT_CLICK_BLOCK, new SkillTriggerData.BlockTargetData(e.getPosition()));
        });

        playerNode.addListener(PlayerBlockPlaceEvent.class, e -> {
            ItemStack inHand = e.getEntity().getItemInMainHand();
            boolean activated = triggerSkills(e.getPlayer(), inHand, SkillTrigger.RIGHT_CLICK_BLOCK,
                    new SkillTriggerData.BlockTargetData(e.getBlockPosition()));
            if (activated) {
                e.setCancelled(true);
            }
        });

        /* left-click entity --------------------------------------------- */
        entityNode.addListener(EntityAttackEvent.class, e -> {
            if (!(e.getEntity() instanceof Player p)) return;
            ItemStack inHand = p.getItemInMainHand();
            triggerSkills(p, inHand, SkillTrigger.LEFT_CLICK_ENTITY, new SkillTriggerData.EntityTargetData(e.getTarget()));
        });

        /* left-click air (arm-swing with no victim) --------------------- */
        playerNode.addListener(PlayerHandAnimationEvent.class, e -> {
            Player p = e.getPlayer();
            PlayerHand h = e.getHand();
            ItemStack stack = p.getItemInHand(h);
            triggerSkills(p, stack, SkillTrigger.LEFT_CLICK_AIR, new SkillTriggerData.SimpleData());
        });

        /* tick while held ----------------------------------------------- */
        entityNode.addListener(EntityTickEvent.class, e -> {
            if (!(e.getEntity() instanceof Player p)) return;
            ItemStack inHand = p.getItemInMainHand();
            triggerSkills(p, inHand, SkillTrigger.HELD_TICK, new SkillTriggerData.HeldTickData(e.getEntity().getAliveTicks()));
        });
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */
    private static boolean triggerSkills(Player player,
                                         ItemStack stack,
                                         SkillTrigger trigger,
                                         SkillTriggerData data) {
        if (player == null || stack == null || stack.isAir()) {
            return false;
        }
        GameItem gi = ItemUtils.resolve(stack);
        if (gi == null || !gi.hasSkills()) {
            return false;
        }
        boolean activated = false;
        for (var skill : gi.skills()) {
            activated |= skill.tryActivate(player, stack, trigger, data);
        }
        return activated;
    }

    private ItemEventsCustom() {}
}

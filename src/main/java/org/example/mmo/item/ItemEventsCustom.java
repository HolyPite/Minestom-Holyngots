// org.example.mmo.combats.CustomItemEvents
package org.example.mmo.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.skill.SkillActivationResult;
import org.example.mmo.item.skill.SkillTrigger;
import org.example.mmo.item.skill.SkillTriggerData;

import java.util.Locale;

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
            SkillTriggerOutcome outcome = triggerSkills(e.getPlayer(), clicked, SkillTrigger.INVENTORY_CLICK, new SkillTriggerData.InventoryClickData(e));
            if (!outcome.activated() && outcome.onCooldown()) {
                notifyCooldown(e.getPlayer(), outcome);
            }
        });

        /* Inventory Change ------------------------------------------------*/
        inventoryNode.addListener(InventoryItemChangeEvent.class, e -> {
            var data = new SkillTriggerData.InventoryChangeData(e, e.getPreviousItem(), e.getNewItem());

            AbstractInventory inv = e.getInventory();

            if (inv.getViewers().isEmpty()) return;
            Player player = inv.getViewers().iterator().next();

            SkillTriggerOutcome previousOutcome = triggerSkills(player, e.getPreviousItem(), SkillTrigger.INVENTORY_CHANGE, data);
            SkillTriggerOutcome newOutcome = triggerSkills(player, e.getNewItem(), SkillTrigger.INVENTORY_CHANGE, data);
            if (!previousOutcome.activated() && previousOutcome.onCooldown()) {
                notifyCooldown(player, previousOutcome);
            }
            if (!newOutcome.activated() && newOutcome.onCooldown()) {
                notifyCooldown(player, newOutcome);
            }
        });

        /* right-click air ------------------------------------------------ */
        playerNode.addListener(PlayerUseItemEvent.class, e -> {
            SkillTriggerOutcome outcome = triggerSkills(e.getPlayer(), e.getItemStack(), SkillTrigger.RIGHT_CLICK_AIR, new SkillTriggerData.SimpleData());
            if (outcome.activated()) {
                e.setCancelled(true);
            } else if (outcome.onCooldown()) {
                notifyCooldown(e.getPlayer(), outcome);
            }
        });

        /* right-click block --------------------------------------------- */
        playerNode.addListener(PlayerUseItemOnBlockEvent.class, e -> {
            ItemStack inHand = e.getItemStack();
            SkillTriggerOutcome outcome = triggerSkills(e.getPlayer(), inHand, SkillTrigger.RIGHT_CLICK_BLOCK, new SkillTriggerData.BlockTargetData(e.getPosition()));
            if (!outcome.activated() && outcome.onCooldown()) {
                notifyCooldown(e.getPlayer(), outcome);
            }
        });

        playerNode.addListener(PlayerBlockPlaceEvent.class, e -> {
            ItemStack inHand = e.getEntity().getItemInMainHand();
            SkillTriggerOutcome outcome = triggerSkills(e.getPlayer(), inHand, SkillTrigger.RIGHT_CLICK_BLOCK,
                    new SkillTriggerData.BlockTargetData(e.getBlockPosition()));
            if (outcome.activated()) {
                e.setCancelled(true);
            } else if (outcome.onCooldown()) {
                notifyCooldown(e.getPlayer(), outcome);
            }
        });

        /* left-click entity --------------------------------------------- */
        entityNode.addListener(EntityAttackEvent.class, e -> {
            if (!(e.getEntity() instanceof Player p)) return;
            ItemStack inHand = p.getItemInMainHand();
            SkillTriggerOutcome outcome = triggerSkills(p, inHand, SkillTrigger.LEFT_CLICK_ENTITY, new SkillTriggerData.EntityTargetData(e.getTarget()));
            if (!outcome.activated() && outcome.onCooldown()) {
                notifyCooldown(p, outcome);
            }
        });

        /* left-click air (arm-swing with no victim) --------------------- */
        playerNode.addListener(PlayerHandAnimationEvent.class, e -> {
            Player p = e.getPlayer();
            PlayerHand h = e.getHand();
            ItemStack stack = p.getItemInHand(h);
            SkillTriggerOutcome outcome = triggerSkills(p, stack, SkillTrigger.LEFT_CLICK_AIR, new SkillTriggerData.SimpleData());
            if (!outcome.activated() && outcome.onCooldown()) {
                notifyCooldown(p, outcome);
            }
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
    private static SkillTriggerOutcome triggerSkills(Player player,
                                                     ItemStack stack,
                                                     SkillTrigger trigger,
                                                     SkillTriggerData data) {
        if (player == null || stack == null || stack.isAir()) {
            return SkillTriggerOutcome.EMPTY;
        }
        GameItem gi = ItemUtils.resolve(stack);
        if (gi == null || !gi.hasSkills()) {
            return SkillTriggerOutcome.EMPTY;
        }
        boolean activated = false;
        long cooldownMs = 0L;
        String cooldownPowerId = null;
        for (var skill : gi.skills()) {
            SkillActivationResult result = skill.tryActivate(player, stack, trigger, data);
            if (result.isSuccess()) {
                activated = true;
            } else if (result.isOnCooldown() && result.cooldownRemainingMs() > cooldownMs) {
                cooldownMs = result.cooldownRemainingMs();
                cooldownPowerId = result.powerId();
            }
        }
        return new SkillTriggerOutcome(activated, cooldownMs, cooldownPowerId);
    }

    private static void notifyCooldown(Player player, SkillTriggerOutcome outcome) {
        if (player == null || !outcome.onCooldown()) {
            return;
        }
        double seconds = outcome.cooldownRemainingMs() / 1000.0;
        String formatted = String.format(Locale.US, "%.1f", seconds);
        String label = prettifyPowerId(outcome.cooldownPowerId());
        Component message = Component.text("[" + label + "] ", NamedTextColor.DARK_AQUA)
                .append(Component.text("Recharge " + formatted + "s", NamedTextColor.GRAY));
        player.sendActionBar(message);
    }

    private static String prettifyPowerId(String powerId) {
        if (powerId == null || powerId.isBlank()) {
            return "Pouvoir";
        }
        int colon = powerId.indexOf(':');
        String tail = colon >= 0 ? powerId.substring(colon + 1) : powerId;
        tail = tail.replace('_', ' ');
        if (tail.isEmpty()) {
            return "Pouvoir";
        }
        return Character.toUpperCase(tail.charAt(0)) + tail.substring(1);
    }

    private record SkillTriggerOutcome(boolean activated, long cooldownRemainingMs, String cooldownPowerId) {
        private static final SkillTriggerOutcome EMPTY = new SkillTriggerOutcome(false, 0L, null);

        boolean onCooldown() {
            return cooldownRemainingMs > 0L;
        }
    }

    private ItemEventsCustom() {}
}

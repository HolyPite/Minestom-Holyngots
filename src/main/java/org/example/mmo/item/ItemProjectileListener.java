package org.example.mmo.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem.AmmoOptions;
import org.example.mmo.item.GameItem.ProjectileOptions;
import org.example.mmo.item.GameItem.ProjectileOptions.Trigger;
import org.example.mmo.item.datas.AmmoType;
import org.example.mmo.projectile.ProjectileLaunchConfig;
import org.example.mmo.projectile.ProjectileLauncher;

final class ItemProjectileListener {

    private static final double DEFAULT_RANGE = 24.0D;
    private static final long DEFAULT_MAX_USE_TICKS = 72000L;

    private static final Map<UUID, Map<String, ShotState>> LAST_SHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, ChargeState> CHARGE_STATES = new ConcurrentHashMap<>();

    private ItemProjectileListener() {
    }

    static void init(EventNode<Event> events, EventNode<PlayerEvent> playerNode) {
        playerNode.addListener(PlayerUseItemEvent.class, ItemProjectileListener::handleUseEvent);

        playerNode.addListener(PlayerHandAnimationEvent.class, event ->
                processAction(event.getPlayer(), event.getHand(), Action.LEFT_CLICK, event.getPlayer().getItemInHand(event.getHand()), 0));

        playerNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (processAction(event.getPlayer(), PlayerHand.MAIN, Action.RIGHT_CLICK, event.getPlayer().getItemInMainHand(), 0)) {
                event.setCancelled(true);
            }
        });

        playerNode.addListener(PlayerDisconnectEvent.class, event -> {
            UUID uuid = event.getPlayer().getUuid();
            LAST_SHOTS.remove(uuid);
            CHARGE_STATES.remove(uuid);
        });

        events.addListener(PlayerFinishItemUseEvent.class, ItemProjectileListener::processFinishUse);
        events.addListener(PlayerTickEvent.class, event -> handleChargeTick(event.getPlayer()));
    }

    private static void handleUseEvent(PlayerUseItemEvent event) {
        if (processUseEvent(event)) {
            event.setCancelled(true);
        }
    }

    private static boolean processUseEvent(PlayerUseItemEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isAir()) {
            return false;
        }
        GameItem item = ItemUtils.resolve(stack);
        if (item == null) {
            return false;
        }
        ProjectileOptions options = item.projectileOptions().orElse(null);
        if (options == null) {
            return false;
        }

        Trigger trigger = options.trigger();
        if (trigger == Trigger.USE_RELEASE) {
            if (!hasAmmo(player, options)) {
                sendMissingAmmoMessage(player, options);
                return true;
            }
            startCharge(player, event.getHand(), stack, item, options);
            return true;
        }

        Action action = trigger == Trigger.USE_HELD ? Action.USE_HELD : Action.RIGHT_CLICK;
        return processAction(player, event.getHand(), action, stack, item, options, 0);
    }

    private static void startCharge(Player player, PlayerHand hand, ItemStack stack, GameItem item, ProjectileOptions options) {
        long useTicks = options.chargeTicks() > 0 ? options.chargeTicks() : DEFAULT_MAX_USE_TICKS;
        boolean offHand = hand == PlayerHand.OFF;
        player.refreshActiveHand(true, offHand, false);
        player.refreshItemUse(hand, useTicks);
        CHARGE_STATES.put(player.getUuid(), new ChargeState(item.getId(), hand, options, player.getAliveTicks()));
    }

    private static void processFinishUse(PlayerFinishItemUseEvent event) {
        Player player = event.getPlayer();
        ChargeState state = CHARGE_STATES.remove(player.getUuid());
        if (state == null) {
            return;
        }

        ItemStack stack = event.getItemStack();
        GameItem item = ItemUtils.resolve(stack);
        if (item == null || !item.getId().equals(state.itemId())) {
            return;
        }
        ProjectileOptions options = item.projectileOptions().orElse(null);
        if (options == null) {
            return;
        }

        long durationTicks = event.getUseDuration();
        if (durationTicks <= 0) {
            durationTicks = Math.max(0L, player.getAliveTicks() - state.startTick());
        }

        processAction(player, state.hand(), Action.USE_RELEASE, stack, item, options, durationTicks);
    }

    private static void handleChargeTick(Player player) {
        ChargeState state = CHARGE_STATES.get(player.getUuid());
        if (state == null) {
            return;
        }
        if (player.isUsingItem()) {
            return;
        }

        CHARGE_STATES.remove(player.getUuid());
        long chargeSpent = Math.max(0L, player.getAliveTicks() - state.startTick());
        ItemStack stack = player.getItemInHand(state.hand());
        processAction(player, state.hand(), Action.USE_RELEASE, stack, chargeSpent);
        player.refreshActiveHand(false, state.hand() == PlayerHand.OFF, false);
        player.refreshItemUse(null, 0);
    }

    private static boolean processAction(Player player,
                                         PlayerHand hand,
                                         Action action,
                                         ItemStack stack,
                                         long chargeTicksSpent) {
        GameItem item = ItemUtils.resolve(stack);
        if (item == null) {
            return false;
        }
        ProjectileOptions options = item.projectileOptions().orElse(null);
        if (options == null) {
            return false;
        }
        return processAction(player, hand, action, stack, item, options, chargeTicksSpent);
    }

    private static boolean processAction(Player player,
                                         PlayerHand hand,
                                         Action action,
                                         ItemStack stack,
                                         GameItem item,
                                         ProjectileOptions options,
                                         long chargeTicksSpent) {
        if (!options.allowOffHand() && hand == PlayerHand.OFF) {
            return false;
        }
        if (!isActionCompatible(options.trigger(), action)) {
            return false;
        }

        double range = options.range() > 0 ? options.range() : DEFAULT_RANGE;
        Pos target = computeAim(player, range);
        double chargeMultiplier = computeChargeMultiplier(options, chargeTicksSpent);
        ProjectileLaunchConfig config = options.toLaunchConfig(chargeMultiplier);

        ShotState state = LAST_SHOTS
                .computeIfAbsent(player.getUuid(), uuid -> new ConcurrentHashMap<>())
                .computeIfAbsent(item.getId(), key -> new ShotState());

        long currentTick = player.getAliveTicks();
        if (!state.canShoot(currentTick, options.cooldownTicks())) {
            return false;
        }

        if (!consumeAmmo(player, options)) {
            sendMissingAmmoMessage(player, options);
            return false;
        }

        EntityProjectile projectile = ProjectileLauncher.launchTowards(player, target, config);
        if (projectile == null) {
            return false;
        }

        state.mark(currentTick);
        return true;
    }

    private static double computeChargeMultiplier(ProjectileOptions options, long chargeTicksSpent) {
        if (options.chargeTicks() <= 0) {
            return 1.0D;
        }
        double ratio = Math.min(1.0D, chargeTicksSpent / (double) options.chargeTicks());
        return Math.max(0.25D, ratio);
    }

    private static boolean isActionCompatible(Trigger trigger, Action action) {
        return switch (trigger) {
            case LEFT_CLICK -> action == Action.LEFT_CLICK;
            case RIGHT_CLICK -> action == Action.RIGHT_CLICK;
            case BOTH -> action == Action.LEFT_CLICK || action == Action.RIGHT_CLICK;
            case USE_RELEASE -> action == Action.USE_RELEASE;
            case USE_HELD -> action == Action.USE_HELD;
        };
    }

    private static Pos computeAim(Player player, double range) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = player.getPosition().direction();
        if (direction.lengthSquared() < 1e-6) {
            direction = new Vec(0, 0, 1);
        } else {
            direction = direction.normalize();
        }
        return eye.add(direction.mul(range));
    }

    private static boolean hasAmmo(Player player, ProjectileOptions options) {
        if (!options.consumesAmmo()) {
            return true;
        }
        return locateAmmoSlots(player, options) != null;
    }

    private static boolean consumeAmmo(Player player, ProjectileOptions options) {
        if (!options.consumesAmmo()) {
            return true;
        }

        List<SlotUse> slots = locateAmmoSlots(player, options);
        if (slots == null) {
            return false;
        }
        if (slots.isEmpty()) {
            return true;
        }

        PlayerInventory inventory = player.getInventory();
        for (SlotUse use : slots) {
            ItemStack stack = inventory.getItemStack(use.slot());
            if (stack == null || stack.isAir()) {
                continue;
            }
            int updated = stack.amount() - use.amount();
            inventory.setItemStack(use.slot(), updated <= 0 ? ItemStack.AIR : stack.withAmount(updated));
        }
        return true;
    }

    private static List<SlotUse> locateAmmoSlots(Player player, ProjectileOptions options) {
        if (!options.consumesAmmo()) {
            return Collections.emptyList();
        }
        AmmoType ammoType = options.ammoType();
        int remaining = options.ammoPerShot();
        if (ammoType == null || remaining <= 0) {
            return Collections.emptyList();
        }

        PlayerInventory inventory = player.getInventory();
        List<SlotUse> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack slotStack = inventory.getItemStack(slot);
            if (slotStack == null || slotStack.isAir()) {
                continue;
            }
            GameItem slotItem = ItemUtils.resolve(slotStack);
            if (slotItem == null) {
                continue;
            }
            AmmoType slotType = slotItem.ammoOptions()
                    .map(AmmoOptions::type)
                    .orElse(null);
            if (slotType == null || slotType != ammoType) {
                continue;
            }

            int available = slotStack.amount();
            int take = Math.min(available, remaining);
            result.add(new SlotUse(slot, take));
            remaining -= take;
            if (remaining <= 0) {
                break;
            }
        }
        return remaining <= 0 ? result : null;
    }

    private static void sendMissingAmmoMessage(Player player, ProjectileOptions options) {
        AmmoType type = options.ammoType();
        String label = ammoLabel(type);
        player.sendMessage(Component.text("Plus de munitions " + label + " !", NamedTextColor.RED));
    }

    private static String ammoLabel(AmmoType type) {
        if (type == null) {
            return "inconnue";
        }
        return type.name().toLowerCase();
    }

    private enum Action {
        LEFT_CLICK,
        RIGHT_CLICK,
        USE_HELD,
        USE_RELEASE
    }

    private static final class ShotState {
        private long lastTick = Long.MIN_VALUE;

        boolean canShoot(long currentTick, long cooldownTicks) {
            long minGap = Math.max(1L, cooldownTicks);
            return lastTick == Long.MIN_VALUE || currentTick - lastTick >= minGap;
        }

        void mark(long currentTick) {
            this.lastTick = currentTick;
        }
    }

    private record SlotUse(int slot, int amount) {
    }

    private record ChargeState(String itemId, PlayerHand hand, ProjectileOptions options, long startTick) {
    }
}

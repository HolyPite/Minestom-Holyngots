package org.example.mmo.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem.ProjectileOptions;
import org.example.mmo.projectile.ProjectileLaunchConfig;
import org.example.mmo.projectile.ProjectileLauncher;

final class ItemProjectileListener {

    private static final double DEFAULT_RANGE = 24.0D;
    private static final Map<UUID, Map<String, ShotState>> LAST_SHOTS = new ConcurrentHashMap<>();

    private ItemProjectileListener() {
    }

    static void init(EventNode<PlayerEvent> playerNode) {
        playerNode.addListener(PlayerUseItemEvent.class, event -> {
            if (handle(event.getPlayer(), event.getHand(), Action.RIGHT_CLICK, event.getItemStack())) {
                event.setCancelled(true);
            }
        });

        playerNode.addListener(PlayerHandAnimationEvent.class, event ->
                handle(event.getPlayer(), event.getHand(), Action.LEFT_CLICK, event.getPlayer().getItemInHand(event.getHand())));

        playerNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (handle(event.getPlayer(), PlayerHand.MAIN, Action.RIGHT_CLICK, event.getPlayer().getItemInMainHand())) {
                event.setCancelled(true);
            }
        });

        playerNode.addListener(PlayerDisconnectEvent.class, event -> LAST_SHOTS.remove(event.getPlayer().getUuid()));
    }

    private static boolean handle(Player player, PlayerHand hand, Action action, ItemStack stack) {
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
        if (!options.allowOffHand() && hand == PlayerHand.OFF) {
            return false;
        }
        boolean leftClick = action == Action.LEFT_CLICK;
        if (!options.acceptsTrigger(leftClick)) {
            return false;
        }

        double range = options.range() > 0 ? options.range() : DEFAULT_RANGE;
        Pos target = computeAim(player, range);
        ProjectileLaunchConfig config = options.toLaunchConfig();

        ShotState state = LAST_SHOTS
                .computeIfAbsent(player.getUuid(), uuid -> new ConcurrentHashMap<>())
                .computeIfAbsent(item.id, key -> new ShotState());

        long currentTick = player.getAliveTicks();
        if (!state.canShoot(currentTick, options.cooldownTicks())) {
            return false;
        }

        EntityProjectile projectile = ProjectileLauncher.launchTowards(player, target, config);
        if (projectile == null) {
            return false;
        }

        state.mark(currentTick);
        return true;
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

    private enum Action {
        LEFT_CLICK,
        RIGHT_CLICK
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
}

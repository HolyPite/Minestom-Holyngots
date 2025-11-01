package org.example.mmo.combat.ui;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.timer.TaskSchedule;
import org.example.mmo.combat.util.HealthUtils;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Displays a temporary boss bar showing the last damaged mob's health.
 */
public final class CombatBossBarService {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Map<UUID, BossBarContext> BARS = new ConcurrentHashMap<>();

    private CombatBossBarService() {
    }

    public static void init(EventNode<Event> eventNode, EventNode<PlayerEvent> playerNode) {
        eventNode.addListener(EntityDamageEvent.class, CombatBossBarService::handleDamage);
        eventNode.addListener(EntityDeathEvent.class, CombatBossBarService::handleEntityDeath);
        playerNode.addListener(PlayerDisconnectEvent.class, event -> clear(event.getPlayer()));

        MinecraftServer.getSchedulerManager()
                .buildTask(CombatBossBarService::tick)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    private static void handleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (victim instanceof Player) {
            return; // Skip player versus player for now
        }
        var damage = event.getDamage();
        if (damage == null || event.isCancelled() || damage.getAmount() <= 0f) {
            return;
        }
        Entity attackerEntity = damage.getAttacker();
        if (!(attackerEntity instanceof Player player)) {
            return;
        }
        if (player == victim) {
            return;
        }
        updateBar(player, victim);
    }

    private static void handleEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        BARS.entrySet().removeIf(entry -> {
            BossBarContext context = entry.getValue();
            if (context.target.getUuid().equals(living.getUuid())) {
                Player owner = context.owner;
                if (owner != null) {
                    owner.hideBossBar(context.bossBar);
                }
                return true;
            }
            return false;
        });
    }

    private static void updateBar(Player player, LivingEntity target) {
        float maxHealth = HealthUtils.resolveMaxHealth(target);
        float health = Math.max(0f, target.getHealth());
        float progress = Math.max(0f, Math.min(1f, maxHealth > 0f ? health / maxHealth : 0f));
        BossBar.Color color = chooseColor(progress);
        Component title = buildTitle(target, health, maxHealth);

        BossBarContext context = BARS.computeIfAbsent(player.getUuid(), uuid -> {
            BossBar bar = BossBar.bossBar(title, progress, color, BossBar.Overlay.PROGRESS);
            player.showBossBar(bar);
            return new BossBarContext(player, target, bar, System.currentTimeMillis());
        });

        if (context.target != target) {
            player.hideBossBar(context.bossBar);
            BossBar bar = BossBar.bossBar(title, progress, color, BossBar.Overlay.PROGRESS);
            player.showBossBar(bar);
            BARS.put(player.getUuid(), new BossBarContext(player, target, bar, System.currentTimeMillis()));
            return;
        }

        context.bossBar.progress(progress);
        context.bossBar.color(color);
        context.bossBar.name(title);
        context.lastTouched = System.currentTimeMillis();
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        BARS.entrySet().removeIf(entry -> {
            BossBarContext context = entry.getValue();
            Player owner = context.owner;
            boolean expired = owner == null
                    || owner.isRemoved()
                    || now - context.lastTouched > TIMEOUT.toMillis()
                    || context.target.isRemoved()
                    || context.target.isDead();
            if (expired) {
                if (owner != null) {
                    owner.hideBossBar(context.bossBar);
                }
            } else {
                float maxHealth = HealthUtils.resolveMaxHealth(context.target);
                float health = Math.max(0f, context.target.getHealth());
                float progress = Math.max(0f, Math.min(1f, maxHealth > 0f ? health / maxHealth : 0f));
                context.bossBar.progress(progress);
                context.bossBar.color(chooseColor(progress));
                context.bossBar.name(buildTitle(context.target, health, maxHealth));
            }
            return expired;
        });
    }

    private static void clear(Player player) {
        BossBarContext context = BARS.remove(player.getUuid());
        if (context != null) {
            player.hideBossBar(context.bossBar);
        }
    }

    private static Component buildTitle(LivingEntity target, float health, float maxHealth) {
        int roundedHealth = Math.round(health);
        int roundedMax = Math.round(maxHealth);
        Component name = target.getCustomName() != null
                ? target.getCustomName()
                : Component.text(target.getEntityType().name().replace('_', ' '), NamedTextColor.WHITE);
        return Component.text()
                .append(name)
                .append(Component.text(" ", NamedTextColor.WHITE))
                .append(Component.text(roundedHealth + "/" + roundedMax + " PV", NamedTextColor.GRAY))
                .build();
    }

    private static BossBar.Color chooseColor(float progress) {
        if (progress > 0.6f) {
            return BossBar.Color.GREEN;
        }
        if (progress > 0.3f) {
            return BossBar.Color.YELLOW;
        }
        return BossBar.Color.RED;
    }

    private static final class BossBarContext {
        private final Player owner;
        private final LivingEntity target;
        private final BossBar bossBar;
        private long lastTouched;

        private BossBarContext(Player owner, LivingEntity target, BossBar bossBar, long lastTouched) {
            this.owner = owner;
            this.target = target;
            this.bossBar = bossBar;
            this.lastTouched = lastTouched;
        }
    }
}

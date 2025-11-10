package org.example.mmo.combat.history;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.example.bootstrap.GameContext;
import org.example.mmo.combat.util.StatUtils;
import org.example.mmo.item.datas.StatType;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A static manager class to track damage history for all living entities.
 */
public final class DamageTracker {

    private static final Map<UUID, DamageHistory> DAMAGE_HISTORY_MAP = new ConcurrentHashMap<>();
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(5);

    private DamageTracker() {
        // Private constructor to prevent instantiation
    }

    public static void init() {
        MinecraftServer.getSchedulerManager()
                .buildTask(DamageTracker::cleanupExpiredHistories)
                .repeat(TaskSchedule.seconds(30))
                .schedule();
    }

    public static void recordDamage(LivingEntity victim, Damage damage) {
        if (damage.getAmount() <= 0f) {
            return;
        }
        var type = damage.getType();
        if (type == null) {
            type = DamageType.GENERIC;
        }
        DamageHistory history = DAMAGE_HISTORY_MAP.computeIfAbsent(victim.getUuid(), k -> new DamageHistory());
        DamageRecord record = new DamageRecord(damage, type, System.currentTimeMillis());
        history.addRecord(record);
    }

    public static DamageHistory getHistory(Entity entity) {
        return DAMAGE_HISTORY_MAP.get(entity.getUuid());
    }

    public static long getLastDamageTimestamp(Entity entity) {
        DamageHistory history = DAMAGE_HISTORY_MAP.get(entity.getUuid());
        return history == null ? 0L : history.getLastDamageTimestamp();
    }

    private static void cleanupExpiredHistories() {
        long now = System.currentTimeMillis();
        DAMAGE_HISTORY_MAP.entrySet().removeIf(entry -> {
            long lastDamageTime = entry.getValue().getLastDamageTimestamp();
            boolean expired = (now - lastDamageTime) > EXPIRATION_TIME.toMillis();

            if (expired) {
                // Find the entity across all game instances
                Entity entity = null;
                for (Instance instance : GameContext.get().instances().gameInstances()) {
                    for (Entity entInInstance : instance.getEntities()) {
                        if (entInInstance.getUuid().equals(entry.getKey())) {
                            entity = entInInstance;
                            break;
                        }
                    }
                    if (entity != null) break;
                }

                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player) && !livingEntity.isDead()) {
                    // Use StatUtils to get max health, consistent with the rest of the system
                    float maxHealth = StatUtils.getTotal(livingEntity, StatType.HEALTH);
                    if (maxHealth <= 0f) {
                        var attribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);
                        if (attribute != null) {
                            maxHealth = (float) attribute.getValue();
                        } else {
                            maxHealth = livingEntity.getHealth();
                        }
                    }
                    livingEntity.setHealth(Math.max(1f, maxHealth));
                }
            }
            return expired;
        });
    }

    public static void clear(Entity entity) {
        DAMAGE_HISTORY_MAP.remove(entity.getUuid());
    }
}

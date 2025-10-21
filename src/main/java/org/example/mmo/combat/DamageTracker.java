package org.example.mmo.combat;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.timer.TaskSchedule;

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

    /**
     * Initializes the DamageTracker system.
     * Starts a periodic task to clean up expired damage histories.
     */
    public static void init() {
        MinecraftServer.getSchedulerManager()
                .buildTask(DamageTracker::cleanupExpiredHistories)
                .repeat(TaskSchedule.seconds(30)) // Check for cleanup every 30 seconds
                .schedule();
    }

    /**
     * Records a new damage event for a specific entity.
     * @param victim The entity that was damaged.
     * @param damage The damage object.
     */
    public static void recordDamage(LivingEntity victim, Damage damage) {
        DamageHistory history = DAMAGE_HISTORY_MAP.computeIfAbsent(victim.getUuid(), k -> new DamageHistory());
        DamageRecord record = new DamageRecord(damage, System.currentTimeMillis());
        history.addRecord(record);
    }

    /**
     * Retrieves the damage history for a given entity.
     * @param entity The entity whose history to retrieve.
     * @return The DamageHistory object, or null if none exists.
     */
    public static DamageHistory getHistory(Entity entity) {
        return DAMAGE_HISTORY_MAP.get(entity.getUuid());
    }

    /**
     * The cleanup task that runs periodically to remove old damage histories.
     */
    private static void cleanupExpiredHistories() {
        long now = System.currentTimeMillis();
        DAMAGE_HISTORY_MAP.entrySet().removeIf(entry -> {
            long lastDamageTime = entry.getValue().getLastDamageTimestamp();
            boolean expired = (now - lastDamageTime) > EXPIRATION_TIME.toMillis();

            if (expired) {
                Entity entity = Entity.getEntity(entry.getKey());
                // If the entity is still loaded, is not a player, and is alive, heal it to full.
                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player) && !livingEntity.isDead()) {
                    livingEntity.setHealth(livingEntity.getMaxHealth());
                }
            }
            return expired;
        });
    }
}

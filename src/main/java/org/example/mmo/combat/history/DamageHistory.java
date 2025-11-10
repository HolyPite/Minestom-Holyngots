package org.example.mmo.combat.history;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the full damage history for a single entity.
 */
public class DamageHistory {

    private static final int MAX_RECORDS = 64;

    private final Deque<DamageRecord> records = new ArrayDeque<>(MAX_RECORDS);
    private final Map<UUID, AttackerStats> attackerStats = new ConcurrentHashMap<>();
    private long lastDamageTimestamp;
    private volatile UUID lastPlayerAttacker;

    public DamageHistory() {
        this.lastDamageTimestamp = System.currentTimeMillis();
    }

    /**
     * Adds a new damage record to this entity's history.
     * @param record The DamageRecord to add.
     */
    public void addRecord(DamageRecord record) {
        synchronized (records) {
            if (records.size() >= MAX_RECORDS) {
                records.removeFirst();
            }
            records.addLast(record);
            this.lastDamageTimestamp = record.timestamp();
        }
        recordContribution(record);
    }

    /**
     * Gets the timestamp of the last damage event.
     * @return The timestamp in milliseconds.
     */
    public long getLastDamageTimestamp() {
        return lastDamageTimestamp;
    }

    /**
     * Gets the last damage record, if any.
     * @return The last DamageRecord, or null if the history is empty.
     */
    public DamageRecord getLastDamage() {
        synchronized (records) {
            return records.peekLast();
        }
    }

    /**
     * Utility method to find the player who was the last attacker in the history.
     * @return The last Player attacker, or null if none is found.
     */
    public Player findLastPlayerAttacker() {
        UUID uuid = lastPlayerAttacker;
        if (uuid == null) {
            return null;
        }
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
    }

    /**
     * Returns an unmodifiable view of the damage records.
     * @return A list of all damage records.
     */
    public List<DamageRecord> getRecords() {
        synchronized (records) {
            return List.copyOf(records);
        }
    }

    /**
     * Returns a snapshot of the aggregated contributions for all attackers.
     */
    public List<AttackerContribution> getAttackerContributions() {
        List<AttackerContribution> snapshot = new ArrayList<>(attackerStats.size());
        for (AttackerStats stats : attackerStats.values()) {
            snapshot.add(stats.snapshot());
        }
        return snapshot;
    }

    private void recordContribution(DamageRecord record) {
        Damage damage = record.damage();
        if (damage == null) {
            return;
        }
        Entity attacker = damage.getAttacker();
        if (!(attacker instanceof LivingEntity living)) {
            return;
        }
        float amount = damage.getAmount();
        if (amount <= 0f) {
            return;
        }
        UUID attackerId = living.getUuid();
        attackerStats.compute(attackerId, (uuid, stats) -> {
            if (stats == null) {
                stats = new AttackerStats(uuid);
            }
            stats.record(amount, resolveTypeName(record.type()), record.timestamp());
            return stats;
        });
        if (attacker instanceof Player player) {
            lastPlayerAttacker = player.getUuid();
        }
    }

    private static String resolveTypeName(RegistryKey<DamageType> type) {
        return type == null ? "minecraft:generic" : type.name();
    }

    public record AttackerContribution(UUID attackerId,
                                       double totalDamage,
                                       Map<String, Double> damageByType,
                                       long lastHitTimestamp) {
    }

    private static final class AttackerStats {
        private final UUID attackerId;
        private double totalDamage;
        private final Map<String, Double> damageByType = new ConcurrentHashMap<>();
        private long lastHitTimestamp;

        private AttackerStats(UUID attackerId) {
            this.attackerId = attackerId;
        }

        private void record(double amount, String damageType, long timestamp) {
            this.totalDamage += amount;
            damageByType.merge(damageType, (double) amount, Double::sum);
            this.lastHitTimestamp = Math.max(this.lastHitTimestamp, timestamp);
        }

        private AttackerContribution snapshot() {
            return new AttackerContribution(attackerId, totalDamage, Map.copyOf(damageByType), lastHitTimestamp);
        }
    }
}

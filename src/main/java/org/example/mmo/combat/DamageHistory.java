package org.example.mmo.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the full damage history for a single entity.
 */
public class DamageHistory {

    private final List<DamageRecord> records = new ArrayList<>();
    private long lastDamageTimestamp;

    public DamageHistory() {
        this.lastDamageTimestamp = System.currentTimeMillis();
    }

    /**
     * Adds a new damage record to this entity's history.
     * @param record The DamageRecord to add.
     */
    public void addRecord(DamageRecord record) {
        synchronized (records) {
            records.add(record);
            this.lastDamageTimestamp = record.timestamp();
        }
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
            if (records.isEmpty()) {
                return null;
            }
            return records.getLast();
        }
    }

    /**
     * Utility method to find the player who was the last attacker in the history.
     * @return The last Player attacker, or null if none is found.
     */
    public Player findLastPlayerAttacker() {
        synchronized (records) {
            // Iterate backwards to find the most recent attacker
            for (int i = records.size() - 1; i >= 0; i--) {
                Entity attacker = records.get(i).damage().getAttacker();
                if (attacker instanceof Player player) {
                    return player;
                }
            }
            return null;
        }
    }

    /**
     * Returns an unmodifiable view of the damage records.
     * @return A list of all damage records.
     */
    public List<DamageRecord> getRecords() {
        synchronized (records) {
            return Collections.unmodifiableList(records);
        }
    }
}

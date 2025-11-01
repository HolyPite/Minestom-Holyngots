package org.example.mmo.combat.history;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Stores the full damage history for a single entity.
 */
public class DamageHistory {

    private static final int MAX_RECORDS = 64;

    private final Deque<DamageRecord> records = new ArrayDeque<>(MAX_RECORDS);
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
            if (records.size() >= MAX_RECORDS) {
                records.removeFirst();
            }
            records.addLast(record);
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
            return records.peekLast();
        }
    }

    /**
     * Utility method to find the player who was the last attacker in the history.
     * @return The last Player attacker, or null if none is found.
    */
    public Player findLastPlayerAttacker() {
        synchronized (records) {
            Iterator<DamageRecord> iterator = records.descendingIterator();
            while (iterator.hasNext()) {
                DamageRecord record = iterator.next();
                Entity attacker = record.damage().getAttacker();
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
            return List.copyOf(records);
        }
    }
}

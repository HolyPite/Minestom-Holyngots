package org.example.mmo.combat.history;

import net.minestom.server.entity.damage.Damage;

/**
 * A simple record to store a single damage event and its timestamp.
 *
 * @param damage The Damage object from Minestom.
 * @param timestamp The time the damage was dealt (System.currentTimeMillis()).
 */
public record DamageRecord(Damage damage, long timestamp) {
}

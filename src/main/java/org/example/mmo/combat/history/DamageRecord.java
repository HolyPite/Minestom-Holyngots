package org.example.mmo.combat.history;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

/**
 * A simple record to store a single damage event, its type and timestamp.
 *
 * @param damage    The Damage object from Minestom.
 * @param type      The resolved damage type key.
 * @param timestamp The time the damage was dealt (System.currentTimeMillis()).
 */
public record DamageRecord(Damage damage, RegistryKey<DamageType> type, long timestamp) {
}

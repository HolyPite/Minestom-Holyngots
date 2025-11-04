package org.example.mmo.mob.behaviour;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import org.example.mmo.mob.MobInstance;

/**
 * Contract for custom behaviour strategies that augment Minestom AI goals.
 */
public interface MobBehaviour {

    default void onSpawn(MobInstance instance) {
    }

    default void onTick(MobInstance instance, long tickTime) {
    }

    default void onAggro(MobInstance instance, Entity target) {
    }

    default void onDamaged(MobInstance instance, Damage damage) {
    }

    default void onDeath(MobInstance instance, Entity killer) {
    }

    default void onCleanup(MobInstance instance) {
    }
}

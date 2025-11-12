package org.example.mmo.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import org.example.mmo.mob.behaviour.MobBehaviour;

/**
 * Convenience adapter offering empty implementations for behaviour hooks.
 */
public abstract class MobBehaviourAdapter implements MobBehaviour {

    @Override
    public void onSpawn(MobInstance instance) {
    }

    @Override
    public void onTick(MobInstance instance, long tickTime) {
    }

    @Override
    public void onAggro(MobInstance instance, Entity target) {
    }

    @Override
    public void onDamaged(MobInstance instance, Damage damage) {
    }

    @Override
    public void onDeath(MobInstance instance, Entity killer) {
    }

    @Override
    public void onCleanup(MobInstance instance) {
    }
}

package org.example.mmo.mob.behaviour;

import net.minestom.server.entity.LivingEntity;
import org.example.mmo.mob.MobArchetype;

/**
 * Factory responsible for creating a behaviour instance per spawned mob.
 */
@FunctionalInterface
public interface MobBehaviourFactory {

    MobBehaviour create(MobArchetype archetype, LivingEntity entity);
}

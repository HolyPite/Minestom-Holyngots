package org.example.mmo.npc.mob.behaviour;

import net.minestom.server.entity.LivingEntity;
import org.example.mmo.npc.mob.MobArchetype;

/**
 * Factory responsible for creating a behaviour instance per spawned mob.
 */
@FunctionalInterface
public interface MobBehaviourFactory {

    MobBehaviour create(MobArchetype archetype, LivingEntity entity);
}

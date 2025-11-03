package org.example.mmo.npc.mob;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.EntityAIGroup;

@FunctionalInterface
public interface MobAiFactory {

    EntityAIGroup build(LivingEntity entity);
}

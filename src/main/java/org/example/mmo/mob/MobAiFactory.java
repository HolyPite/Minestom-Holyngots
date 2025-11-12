package org.example.mmo.mob;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.EntityAIGroup;

@FunctionalInterface
public interface MobAiFactory {

    EntityAIGroup build(LivingEntity entity);
}

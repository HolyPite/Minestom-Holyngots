package org.example.mmo.npc.mob.ai.ais;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import org.example.mmo.npc.mob.MobAiFactory;

/**
 * Simple AI keeping the mob idle while looking around.
 */
public final class PassiveSentryAiFactory implements MobAiFactory {

    public static final PassiveSentryAiFactory INSTANCE = new PassiveSentryAiFactory();

    private PassiveSentryAiFactory() {
    }

    @Override
    public EntityAIGroup build(LivingEntity entity) {
        EntityCreature creature = asCreature(entity);
        if (creature == null) {
            return null;
        }
        return new EntityAIGroupBuilder()
                .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                .build();
    }

    private EntityCreature asCreature(LivingEntity entity) {
        return (entity instanceof EntityCreature creature) ? creature : null;
    }
}

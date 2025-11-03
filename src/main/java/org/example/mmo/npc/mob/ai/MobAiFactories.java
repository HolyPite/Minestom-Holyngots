package org.example.mmo.npc.mob.ai;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.npc.mob.MobAiFactory;

/**
 * Common AI presets for mobs.
 */
public final class MobAiFactories {

    private MobAiFactories() {
    }

    public static MobAiFactory passiveSentry() {
        return entity -> {
            EntityCreature creature = asCreature(entity);
            if (creature == null) {
                return null;
            }
            return new EntityAIGroupBuilder()
                    .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                    .build();
        };
    }

    public static MobAiFactory meleeCharger(double attackSpeed, int attackDelayTicks) {
        return entity -> {
            EntityCreature creature = asCreature(entity);
            if (creature == null) {
                return null;
            }
            return new EntityAIGroupBuilder()
                    .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                    .addGoalSelector(new MeleeAttackGoal(creature, attackSpeed, attackDelayTicks, TimeUnit.SERVER_TICK))
                    .addTargetSelector(new ClosestEntityTarget(creature, 32f, target -> target instanceof Player))
                    .build();
        };
    }

    private static EntityCreature asCreature(LivingEntity entity) {
        return (entity instanceof EntityCreature creature) ? creature : null;
    }
}

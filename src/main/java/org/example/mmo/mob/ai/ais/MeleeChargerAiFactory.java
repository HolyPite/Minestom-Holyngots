package org.example.mmo.mob.ai.ais;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.mob.MobAiFactory;

/**
 * Close-range AI aggressively charging players.
 */
public final class MeleeChargerAiFactory implements MobAiFactory {

    private final double attackSpeed;
    private final int attackDelayTicks;

    public MeleeChargerAiFactory(double attackSpeed, int attackDelayTicks) {
        this.attackSpeed = attackSpeed;
        this.attackDelayTicks = attackDelayTicks;
    }

    @Override
    public EntityAIGroup build(LivingEntity entity) {
        EntityCreature creature = asCreature(entity);
        if (creature == null) {
            return null;
        }
        return new EntityAIGroupBuilder()
                .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                .addGoalSelector(new MeleeAttackGoal(creature, attackSpeed, attackDelayTicks, TimeUnit.SERVER_TICK))
                .addTargetSelector(new ClosestEntityTarget(creature, 32f, target -> target instanceof Player))
                .build();
    }

    private EntityCreature asCreature(LivingEntity entity) {
        return (entity instanceof EntityCreature creature) ? creature : null;
    }
}

package org.example.mmo.npc.mob.ai.ais;

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
 * Patrol-style AI that wanders slowly but becomes aggressive once a player is near.
 */
public final class SunkenGuardianAiFactory implements MobAiFactory {

    private final double attackSpeed;
    private final int attackDelayTicks;
    private final float detectionRange;

    public SunkenGuardianAiFactory(double attackSpeed,
                                   int attackDelayTicks,
                                   float detectionRange) {
        this.attackSpeed = attackSpeed;
        this.attackDelayTicks = attackDelayTicks;
        this.detectionRange = detectionRange;
    }

    @Override
    public EntityAIGroup build(LivingEntity entity) {
        if (!(entity instanceof EntityCreature creature)) {
            return null;
        }
        return new EntityAIGroupBuilder()
                .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                .addGoalSelector(new MeleeAttackGoal(creature, attackSpeed, attackDelayTicks, TimeUnit.SERVER_TICK))
                .addTargetSelector(new ClosestEntityTarget(creature, detectionRange, target -> target instanceof Player))
                .build();
    }
}

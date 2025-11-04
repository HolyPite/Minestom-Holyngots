package org.example.mmo.npc.mob.ai.ais;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.npc.mob.MobAiFactory;
import org.example.mmo.npc.mob.projectile.MobProjectileUtils;

/**
 * Ranged AI using configurable projectile behaviour.
 */
public final class ArcherAiFactory implements MobAiFactory {

    private final int attackDelayTicks;
    private final int attackRange;
    private final int desirableRange;
    private final double projectileSpeed;
    private final double projectileSpread;

    public ArcherAiFactory(int attackDelayTicks,
                           int attackRange,
                           int desirableRange,
                           double projectileSpeed,
                           double projectileSpread) {
        this.attackDelayTicks = attackDelayTicks;
        this.attackRange = attackRange;
        this.desirableRange = desirableRange;
        this.projectileSpeed = projectileSpeed;
        this.projectileSpread = projectileSpread;
    }

    @Override
    public EntityAIGroup build(LivingEntity entity) {
        EntityCreature creature = asCreature(entity);
        if (creature == null) {
            return null;
        }
        int resolvedAttackRange = Math.max(attackRange, desirableRange);
        int resolvedDesirableRange = Math.min(desirableRange, resolvedAttackRange);

        RangedAttackGoal goal = new RangedAttackGoal(
                creature,
                attackDelayTicks,
                resolvedAttackRange,
                resolvedDesirableRange,
                true,
                projectileSpeed,
                projectileSpread,
                TimeUnit.SERVER_TICK
        );
        goal.setProjectileGenerator(target -> MobProjectileUtils.shootArrow(creature, target, projectileSpeed, projectileSpread));
        return new EntityAIGroupBuilder()
                .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                .addGoalSelector(goal)
                .addTargetSelector(new ClosestEntityTarget(creature, resolvedAttackRange, target -> target instanceof Player))
                .build();
    }

    private EntityCreature asCreature(LivingEntity entity) {
        return (entity instanceof EntityCreature creature) ? creature : null;
    }
}

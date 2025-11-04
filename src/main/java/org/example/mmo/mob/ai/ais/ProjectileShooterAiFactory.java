package org.example.mmo.mob.ai.ais;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.example.mmo.mob.MobAiFactory;
import org.example.mmo.mob.projectile.MobProjectileUtils;

/**
 * Generic ranged projectile AI supporting different projectile types.
 */
public final class ProjectileShooterAiFactory implements MobAiFactory {

    private final int attackDelayTicks;
    private final int attackRange;
    private final int desirableRange;
    private final double projectileSpeed;
    private final double projectileSpread;
    private final boolean hasGravity;
    private final EntityType projectileType;

    public ProjectileShooterAiFactory(int attackDelayTicks,
                                      int attackRange,
                                      int desirableRange,
                                      double projectileSpeed,
                                      double projectileSpread,
                                      boolean hasGravity,
                                      EntityType projectileType) {
        this.attackDelayTicks = attackDelayTicks;
        this.attackRange = attackRange;
        this.desirableRange = desirableRange;
        this.projectileSpeed = projectileSpeed;
        this.projectileSpread = projectileSpread;
        this.hasGravity = hasGravity;
        this.projectileType = projectileType;
    }

    @Override
    public EntityAIGroup build(LivingEntity entity) {
        if (!(entity instanceof EntityCreature creature)) {
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
        goal.setProjectileGenerator(target -> MobProjectileUtils.shootProjectile(
                creature,
                target,
                projectileType,
                projectileSpeed,
                projectileSpread,
                hasGravity
        ));

        return new EntityAIGroupBuilder()
                .addGoalSelector(new RandomLookAroundGoal(creature, 2))
                .addGoalSelector(goal)
                .addTargetSelector(new ClosestEntityTarget(creature, resolvedAttackRange, target -> target instanceof Player))
                .build();
    }
}

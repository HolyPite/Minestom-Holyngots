package org.example.mmo.mob.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;

public final class MobProjectileUtils {

    private MobProjectileUtils() {
    }

    public static EntityProjectile shootArrow(EntityCreature shooter,
                                              Entity target,
                                              double speed,
                                              double inaccuracy) {
        return shootProjectile(shooter, target, EntityType.ARROW, speed, inaccuracy, true);
    }

    public static EntityProjectile shootProjectile(EntityCreature shooter,
                                                   Entity target,
                                                   EntityType projectileType,
                                                   double speed,
                                                   double inaccuracy,
                                                   boolean hasGravity) {
        Pos origin = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        EntityProjectile projectile = new EntityProjectile(shooter, projectileType);
        projectile.setInstance(shooter.getInstance(), origin);
        projectile.setNoGravity(!hasGravity);

        Pos aim = target.getPosition();
        if (target instanceof LivingEntity livingTarget) {
            aim = aim.withY(livingTarget.getPosition().y() + livingTarget.getEyeHeight() * 0.5);
        }

        Vec delta = new Vec(aim.x() - origin.x(), aim.y() - origin.y(), aim.z() - origin.z());
        if (delta.lengthSquared() < 0.0001) {
            delta = new Vec(0, 0.1, 0);
        }
        Vec direction = delta.normalize().mul(0.5);
        Pos adjustedTarget = origin.add(direction);

        projectile.shoot(adjustedTarget, speed, inaccuracy);
        return projectile;
    }
}

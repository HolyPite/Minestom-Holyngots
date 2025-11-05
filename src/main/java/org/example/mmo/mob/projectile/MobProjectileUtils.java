package org.example.mmo.mob.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;

public final class MobProjectileUtils {

    private MobProjectileUtils() {
    }

    public static Entity shootProjectile(EntityCreature shooter,
                                         Entity target,
                                         EntityType projectileType,
                                         double speed,
                                         double inaccuracy,
                                         boolean hasGravity) {
        return shootProjectile(shooter, targetPosition(target), projectileType, speed, inaccuracy, hasGravity);
    }

    public static Entity shootProjectile(EntityCreature shooter,
                                         Pos target,
                                         EntityType projectileType,
                                         double speed,
                                         double inaccuracy,
                                         boolean hasGravity) {
        if (shooter.getInstance() == null) {
            return null;
        }
        Pos shooterPos = shooter.getPosition();
        Vec direction = computeDirection(shooter, target);
        Pos spawnPos = shooterPos.add(direction.mul(0.75)).add(0,shooter.getEyeHeight(),0);

        Pos aim = target;

        AbstractMobProjectile projectile;
        if (projectileType == EntityType.ARROW) {
            projectile = new ArrowMobProjectile(shooter);
        } else if (projectileType == EntityType.SMALL_FIREBALL || projectileType == EntityType.FIREBALL) {
            projectile = new FireballMobProjectile(projectileType, shooter);
        } else {
            projectile = new ThrownItemMobProjectile(projectileType, shooter);
        }

        projectile.setNoGravity(!hasGravity);
        projectile.shoot(spawnPos, aim, speed, inaccuracy);
        return projectile;
    }

    private static Vec computeDirection(EntityCreature shooter, Pos target) {
        Pos origin = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Pos aim = target;
        Vec delta = new Vec(aim.x() - origin.x(), aim.y() - origin.y(), aim.z() - origin.z());
        double length = delta.length();
        if (length < 1e-6) {
            return new Vec(0, 0, 0.1);
        }
        return delta.normalize();
    }

    private static Pos targetPosition(Entity target) {
        Pos aim = target.getPosition();
        if (target instanceof LivingEntity livingTarget) {
            aim = aim.withY(livingTarget.getPosition().y() + livingTarget.getEyeHeight() * 0.5);
        }
        return aim;
    }
}

package org.example.mmo.projectile;

import java.util.Objects;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.example.mmo.mob.projectile.AbstractMobProjectile;
import org.example.mmo.mob.projectile.ArrowMobProjectile;
import org.example.mmo.mob.projectile.FireballMobProjectile;
import org.example.mmo.mob.projectile.ThrownItemMobProjectile;

/**
 * Shared projectile launcher for mobs, players or any living entity.
 */
public final class ProjectileLauncher {

    private static final double DEFAULT_SPAWN_OFFSET = 0.75;

    private ProjectileLauncher() {
    }

    public static EntityProjectile launchAtTarget(LivingEntity shooter,
                                                  Entity target,
                                                  ProjectileLaunchConfig config) {
        Objects.requireNonNull(target, "target");
        return launchTowards(shooter, resolveTargetPosition(target), config);
    }

    public static EntityProjectile launchTowards(LivingEntity shooter,
                                                 Pos aim,
                                                 ProjectileLaunchConfig config) {
        Objects.requireNonNull(shooter, "shooter");
        Objects.requireNonNull(config, "config");
        if (aim == null || shooter.getInstance() == null) {
            return null;
        }

        Pos spawnPos = computeSpawnPosition(shooter, aim);
        AbstractMobProjectile projectile = createProjectile(shooter, config.projectileType());

        projectile.setNoGravity(!config.hasGravity());
        projectile.configureLifetime(config.lifetimeTicks(), config.blockLifetimeTicks());
        projectile.launch(spawnPos, aim, config.speed(), config.spread());
        return projectile;
    }

    public static EntityProjectile launchWithDirection(LivingEntity shooter,
                                                       Vec direction,
                                                       ProjectileLaunchConfig config) {
        Objects.requireNonNull(shooter, "shooter");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(config, "config");
        Vec resolvedDirection = direction.lengthSquared() > 1e-6 ? direction.normalize() : new Vec(0, 0, 1);
        Pos origin = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        double aimDistance = Math.max(config.speed(), 1.0D);
        Pos aim = origin.add(resolvedDirection.mul(aimDistance));
        return launchTowards(shooter, aim, config);
    }

    private static AbstractMobProjectile createProjectile(Entity shooter, EntityType projectileType) {
        if (projectileType == EntityType.ARROW) {
            return new ArrowMobProjectile(shooter);
        } else if (projectileType == EntityType.SMALL_FIREBALL || projectileType == EntityType.FIREBALL) {
            return new FireballMobProjectile(projectileType, shooter);
        }
        return new ThrownItemMobProjectile(projectileType, shooter);
    }

    private static Pos computeSpawnPosition(LivingEntity shooter, Pos aim) {
        Vec direction = computeDirection(shooter, aim);
        Pos base = shooter.getPosition();
        return base.add(direction.mul(DEFAULT_SPAWN_OFFSET)).add(0, shooter.getEyeHeight(), 0);
    }

    private static Vec computeDirection(LivingEntity shooter, Pos aim) {
        Pos origin = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Vec delta = new Vec(
                aim.x() - origin.x(),
                aim.y() - origin.y(),
                aim.z() - origin.z()
        );
        double length = delta.length();
        if (length < 1e-6) {
            return new Vec(0, 0, 0.1);
        }
        return delta.normalize();
    }

    private static Pos resolveTargetPosition(Entity target) {
        Pos aim = target.getPosition();
        if (target instanceof LivingEntity livingTarget) {
            aim = aim.add(0, livingTarget.getEyeHeight() * 0.5, 0);
        }
        return aim;
    }
}

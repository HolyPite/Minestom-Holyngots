package org.example.mmo.mob.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.example.mmo.projectile.ProjectileLaunchConfig;
import org.example.mmo.projectile.ProjectileLauncher;

/**
 * @deprecated Use {@link org.example.mmo.projectile.ProjectileLauncher} instead.
 */
@Deprecated(forRemoval = true)
public final class MobProjectileUtils {

    private MobProjectileUtils() {
    }

    public static EntityProjectile shootProjectile(EntityCreature shooter,
                                                   Entity target,
                                                   EntityType projectileType,
                                                   double speed,
                                                   double inaccuracy,
                                                   boolean hasGravity) {
        ProjectileLaunchConfig config = ProjectileLaunchConfig.builder(projectileType)
                .speed(speed)
                .spread(inaccuracy)
                .hasGravity(hasGravity)
                .build();
        return ProjectileLauncher.launchAtTarget(shooter, target, config);
    }

    public static EntityProjectile shootProjectile(EntityCreature shooter,
                                                   Pos target,
                                                   EntityType projectileType,
                                                   double speed,
                                                   double inaccuracy,
                                                   boolean hasGravity) {
        ProjectileLaunchConfig config = ProjectileLaunchConfig.builder(projectileType)
                .speed(speed)
                .spread(inaccuracy)
                .hasGravity(hasGravity)
                .build();
        return ProjectileLauncher.launchTowards(shooter, target, config);
    }
}

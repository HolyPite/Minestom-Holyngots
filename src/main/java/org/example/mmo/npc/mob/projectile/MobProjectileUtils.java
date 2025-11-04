package org.example.mmo.npc.mob.projectile;

import net.minestom.server.coordinate.Pos;
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
        Pos from = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        EntityProjectile arrow = new EntityProjectile(shooter, EntityType.ARROW);
        arrow.setInstance(shooter.getInstance(), from);

        Pos aim = target.getPosition();
        if (target instanceof LivingEntity livingTarget) {
            aim = aim.withY(aim.y() + livingTarget.getEyeHeight() * 0.5);
        }

        arrow.shoot(aim, speed, inaccuracy);
        return arrow;
    }
}
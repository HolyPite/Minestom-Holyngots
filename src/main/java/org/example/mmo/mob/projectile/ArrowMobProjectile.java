package org.example.mmo.mob.projectile;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Adapted arrow projectile using Atlas movement logic.
 */
public final class ArrowMobProjectile extends AbstractMobProjectile {

    private static final BoundingBox SMALL_BOUNDING_BOX = new BoundingBox(0.01, 0.01, 0.01);
    private static final BoundingBox REGULAR_BOUNDING_BOX = EntityType.ARROW.registry().boundingBox();

    private boolean critical;
    private boolean firstTick = true;

    public ArrowMobProjectile(Entity shooter) {
        super(EntityType.ARROW, shooter);
        setBoundingBox(SMALL_BOUNDING_BOX);
        this.collidesWithEntities = false;
        if (getEntityMeta() instanceof ProjectileMeta projectileMeta) {
            projectileMeta.setShooter(shooter);
        }
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
        if (getEntityMeta() instanceof AbstractArrowMeta arrowMeta) {
            arrowMeta.setCritical(critical);
        }
    }

    @Override
    public void tick(long time) {
        if (isRemoved() || inBlock) {
            return;
        }
        Pos before = getPosition();
        updatePosition(time);
        Pos now = getPosition();

        Vec diff = Vec.fromPoint(now.sub(before));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
        float pitch = (float) Math.toDegrees(Math.atan2(diff.y(),
                Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));
        this.position = now.withView(yaw, pitch);
        if (firstTick) {
            firstTick = false;
            setView(yaw, pitch);
        }

        if (!callEntityCollision(REGULAR_BOUNDING_BOX)) {
            callBlockCollision();
        }
    }

    @Override
    public void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        var instance = shooter.getInstance();
        if (instance == null) {
            return;
        }

        float yaw = -shooter.getPosition().yaw();
        float pitch = -shooter.getPosition().pitch();

        double pitchDiff = pitch - 35f;
        if (pitchDiff == 0) {
            pitchDiff = 0.0001;
        }
        double pitchAdjust = pitchDiff * 0.002145329238474369D;

        double dx = to.x() - from.x();
        double dy = to.y() - from.y() + pitchAdjust;
        double dz = to.z() - from.z();
        if (!hasNoGravity()) {
            final double xzLength = Math.sqrt(dx * dx + dz * dz);
            dy += xzLength * 0.20000000298023224D;
        }

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;

        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, from, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }

        double mul = ServerFlag.SERVER_TICKS_PER_SECOND * power;
        Vec velocity = new Vec(dx * mul, dy * mul * 0.9, dz * mul);

        setInstance(instance, new Pos(from.x(), from.y(), from.z(), yaw, pitch))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        return;
                    }
                    synchronizePosition();
                    setVelocity(velocity);
                    if (critical && getEntityMeta() instanceof AbstractArrowMeta arrowMeta) {
                        arrowMeta.setCritical(true);
                    }
                });
    }

    @Override
    protected @NotNull Vec updateVelocity(@NotNull Pos entityPosition,
                                          @NotNull Vec currentVelocity,
                                          @NotNull Block.Getter blockGetter,
                                          @NotNull Aerodynamics aerodynamics,
                                          boolean positionChanged,
                                          boolean entityFlying,
                                          boolean entityOnGround,
                                          boolean entityNoGravity) {
        if (!positionChanged) {
            return entityFlying ? Vec.ZERO : new Vec(0.0,
                    entityNoGravity ? 0.0 : -aerodynamics.gravity() * aerodynamics.verticalAirResistance(),
                    0.0);
        }
        double drag = entityOnGround
                ? blockGetter.getBlock(entityPosition.sub(0.0, 0.5000001, 0.0)).registry().friction()
                * aerodynamics.horizontalAirResistance()
                : aerodynamics.horizontalAirResistance();
        double gravity = entityFlying ? 0.0 : aerodynamics.gravity();
        double gravityDrag = entityFlying ? 0.6 : aerodynamics.verticalAirResistance();

        double x = currentVelocity.x() * drag;
        double y = entityNoGravity ? currentVelocity.y() : (currentVelocity.y() - gravity) * gravityDrag;
        double z = currentVelocity.z() * drag;
        return new Vec(Math.abs(x) < 1.0E-6 ? 0.0 : x,
                Math.abs(y) < 1.0E-6 ? 0.0 : y,
                Math.abs(z) < 1.0E-6 ? 0.0 : z);
    }
}

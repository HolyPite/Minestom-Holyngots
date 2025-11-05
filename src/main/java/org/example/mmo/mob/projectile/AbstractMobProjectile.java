package org.example.mmo.mob.projectile;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight adaptation of AtlasProjectiles' AbstractProjectile for mob usage.
 */
public abstract class AbstractMobProjectile extends EntityProjectile implements MobProjectile {

    protected final Entity shooter;
    protected PhysicsResult previousPhysicsResult;
    protected Pos previousPosition;
    protected boolean inBlock = false;
    private long maxAliveTicks = -1;
    private long blockLifetimeTicks = -1;
    private long aliveTicks;

    protected AbstractMobProjectile(Entity shooter, EntityType type) {
        super(shooter, type);
        this.shooter = shooter;
    }

    public void configureLifetime(long lifetimeTicks, long blockLifetimeTicks) {
        this.aliveTicks = 0;
        if (lifetimeTicks > 0) {
            scheduleLifetime(lifetimeTicks);
        } else {
            this.maxAliveTicks = -1;
        }
        if (blockLifetimeTicks > 0) {
            scheduleBlockLifetime(blockLifetimeTicks);
        } else {
            this.blockLifetimeTicks = -1;
        }
    }

    @Override
    public void tick(long time) {
        if (isRemoved()) {
            return;
        }
        aliveTicks++;
        if (maxAliveTicks > 0 && aliveTicks >= maxAliveTicks) {
            remove();
            return;
        }
        if (inBlock) {
            return;
        }
        updatePosition(time);
        if (!callEntityCollision()) {
            callBlockCollision();
        }
    }

    @Override
    public void launch(@NotNull Point from, double power, double spread) {
        Point to = from.add(shooter.getPosition().direction());
        launch(from, to, power, spread);
    }

    @Override
    public void launch(@NotNull Point from, @NotNull Point to, double power, double spread) {
        launchInternal(from, to, power, spread);
    }

    protected abstract void launchInternal(@NotNull Point from, @NotNull Point to, double power, double spread);

    protected PhysicsResult computePhysics(@NotNull Pos entityPosition,
                                           @NotNull Vec currentVelocity,
                                           @NotNull Block.Getter blockGetter,
                                           @NotNull Aerodynamics aerodynamics) {
        Vec newVelocity = updateVelocity(entityPosition, currentVelocity, blockGetter, aerodynamics,
                true, false, onGround, false);
        PhysicsResult result = CollisionUtils.handlePhysics(
                blockGetter,
                this.boundingBox,
                entityPosition,
                newVelocity,
                previousPhysicsResult,
                true
        );
        previousPhysicsResult = result;
        return result;
    }

    @Override
    protected void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle != null) {
            return;
        }

        this.previousPosition = position;
        final Block.Getter chunkCache = new ChunkCache(instance, currentChunk);
        PhysicsResult result = computePhysics(
                position,
                velocity.div(ServerFlag.SERVER_TICKS_PER_SECOND),
                chunkCache,
                getAerodynamics()
        );

        Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, result.newPosition());
        if (!ChunkUtils.isLoaded(finalChunk)) {
            return;
        }

        onGround = result.isOnGround();

        if (!result.hasCollision()) {
            velocity = previousPhysicsResult.newVelocity()
                    .mul(ServerFlag.SERVER_TICKS_PER_SECOND)
                    .mul(0.99);
        }

        refreshPosition(result.newPosition(), true, false);
        if (hasVelocity()) {
            sendPacketToViewers(getVelocityPacket());
        }
    }

    protected void callBlockCollision() {
        if (previousPhysicsResult == null || !previousPhysicsResult.hasCollision()) {
            return;
        }
        Block hitBlock = null;
        Point hitPoint = null;
        Shape[] shapes = previousPhysicsResult.collisionShapes();
        Point[] points = previousPhysicsResult.collisionPoints();

        for (int i = 0; i < shapes.length; i++) {
            if (shapes[i] == null || points[i] == null) {
                continue;
            }
            hitBlock = instance.getBlock(points[i].sub(0, Vec.EPSILON, 0), Block.Getter.Condition.TYPE);
            hitPoint = points[i];
            break;
        }

        if (hitBlock == null || hitPoint == null) {
            return;
        }
        handleBlockCollision(hitBlock, hitPoint, previousPosition);
    }

    protected void callBlockCollisionEvent(@NotNull Pos pos, Block hitBlock) {
        ProjectileCollideWithBlockEvent event = new ProjectileCollideWithBlockEvent(this, pos, hitBlock);
        EventDispatcher.call(event);
    }

    protected boolean callEntityCollisionEvent(@NotNull Pos pos, @NotNull Entity entity) {
        ProjectileCollideWithEntityEvent event = new ProjectileCollideWithEntityEvent(this, pos, entity);
        EventDispatcher.call(event);
        if (!event.isCancelled()) {
            remove();
            return true;
        }
        return false;
    }

    protected boolean callEntityCollision() {
        return callEntityCollision(boundingBox);
    }

    protected boolean callEntityCollision(BoundingBox boundingBox) {
        if (previousPhysicsResult == null) {
            return false;
        }
        Vec diff = previousPhysicsResult.newPosition().sub(previousPosition).asVec();
        var collisions = CollisionUtils.checkEntityCollisions(
                instance,
                boundingBox,
                previousPosition,
                diff,
                diff.length(),
                entity -> entity != shooter && entity != this,
                previousPhysicsResult
        ).stream().sorted().toList();

        for (EntityCollisionResult result : collisions) {
            if (handleEntityCollision(result, previousPhysicsResult.newPosition(), previousPosition)) {
                return true;
            }
        }
        return false;
    }

    protected void updatePosition(long time) {
        if (instance == null || isRemoved() || !ChunkUtils.isLoaded(currentChunk)) {
            return;
        }
        movementTick();
        super.update(time);
        EventDispatcher.call(new EntityTickEvent(this));
    }

    protected void handleBlockCollision(Block hitBlock, Point hitPos, Pos posBefore) {
        velocity = Vec.ZERO;
        setNoGravity(true);
        inBlock = true;

        this.position = posBefore.withX(hitPos.x()).withY(hitPos.y()).withZ(hitPos.z());
        MinecraftServer.getSchedulerManager().scheduleNextTick(this::synchronizePosition);

        callBlockCollisionEvent(Pos.fromPoint(hitPos), hitBlock);
        BlockHandler handler = hitBlock.handler();
        if (handler != null) {
            handler.onTouch(new BlockHandler.Touch(hitBlock, instance, hitPos, this));
        }
        maxAliveTicks = -1;
        scheduleRemoval(blockLifetimeTicks);
    }

    protected boolean handleEntityCollision(EntityCollisionResult result, Point hitPos, Pos posBefore) {
        return callEntityCollisionEvent(Pos.fromPoint(hitPos), result.entity());
    }

    protected abstract @NotNull Vec updateVelocity(@NotNull Pos entityPosition,
                                                   @NotNull Vec currentVelocity,
                                                   @NotNull Block.Getter blockGetter,
                                                   @NotNull Aerodynamics aerodynamics,
                                                   boolean positionChanged,
                                                   boolean entityFlying,
                                                   boolean entityOnGround,
                                                   boolean entityNoGravity);

    protected void scheduleLifetime(long lifetimeTicks) {
        this.maxAliveTicks = lifetimeTicks;
    }

    protected void scheduleBlockLifetime(long lifetimeTicks) {
        this.blockLifetimeTicks = lifetimeTicks;
    }

    private void scheduleRemoval(long ticks) {
        if (ticks <= 0) {
            return;
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> {
                    if (!isRemoved()) {
                        remove();
                    }
                })
                .delay(TaskSchedule.tick((int) ticks))
                .schedule();
    }
}

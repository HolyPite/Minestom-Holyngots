package org.example.mmo.projectile;

import java.util.Objects;
import net.minestom.server.entity.EntityType;

/**
 * Describes how to launch a projectile (type, physics, lifetime).
 */
public final class ProjectileLaunchConfig {

    public static final long DEFAULT_LIFETIME_TICKS = 20L * 10; // 10 seconds
    public static final long DEFAULT_BLOCK_LIFETIME_TICKS = 20L * 3; // 3 seconds
    public static final long DEFAULT_ARROW_BLOCK_LIFETIME_TICKS = 20L * 30; // allow recovery

    private final EntityType projectileType;
    private final double speed;
    private final double spread;
    private final boolean hasGravity;
    private final long lifetimeTicks;
    private final long blockLifetimeTicks;

    private ProjectileLaunchConfig(EntityType projectileType,
                                   double speed,
                                   double spread,
                                   boolean hasGravity,
                                   long lifetimeTicks,
                                   long blockLifetimeTicks) {
        this.projectileType = Objects.requireNonNull(projectileType, "projectileType");
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be greater than 0");
        }
        if (spread < 0) {
            throw new IllegalArgumentException("spread cannot be negative");
        }
        this.speed = speed;
        this.spread = spread;
        this.hasGravity = hasGravity;
        this.lifetimeTicks = lifetimeTicks > 0 ? lifetimeTicks : DEFAULT_LIFETIME_TICKS;
        this.blockLifetimeTicks = blockLifetimeTicks >= 0
                ? blockLifetimeTicks
                : defaultBlockLifetime(projectileType);
    }

    public EntityType projectileType() {
        return projectileType;
    }

    public double speed() {
        return speed;
    }

    public double spread() {
        return spread;
    }

    public boolean hasGravity() {
        return hasGravity;
    }

    public long lifetimeTicks() {
        return lifetimeTicks;
    }

    public long blockLifetimeTicks() {
        return blockLifetimeTicks;
    }

    public static Builder builder(EntityType projectileType) {
        return new Builder(projectileType);
    }

    private static long defaultBlockLifetime(EntityType type) {
        return type == EntityType.ARROW
                ? DEFAULT_ARROW_BLOCK_LIFETIME_TICKS
                : DEFAULT_BLOCK_LIFETIME_TICKS;
    }

    public static final class Builder {
        private final EntityType projectileType;
        private double speed = 1.0;
        private double spread = 0.0;
        private boolean hasGravity = true;
        private long lifetimeTicks = DEFAULT_LIFETIME_TICKS;
        private Long blockLifetimeTicks;

        private Builder(EntityType projectileType) {
            this.projectileType = Objects.requireNonNull(projectileType, "projectileType");
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder spread(double spread) {
            this.spread = spread;
            return this;
        }

        public Builder hasGravity(boolean hasGravity) {
            this.hasGravity = hasGravity;
            return this;
        }

        public Builder lifetimeTicks(long lifetimeTicks) {
            this.lifetimeTicks = lifetimeTicks;
            return this;
        }

        public Builder blockLifetimeTicks(long blockLifetimeTicks) {
            this.blockLifetimeTicks = blockLifetimeTicks;
            return this;
        }

        public ProjectileLaunchConfig build() {
            long resolvedBlockLifetime = blockLifetimeTicks != null
                    ? blockLifetimeTicks
                    : defaultBlockLifetime(projectileType);
            long resolvedLifetime = lifetimeTicks > 0 ? lifetimeTicks : DEFAULT_LIFETIME_TICKS;
            double resolvedSpread = spread >= 0 ? spread : 0;
            if (speed <= 0) {
                throw new IllegalArgumentException("speed must be greater than 0");
            }
            return new ProjectileLaunchConfig(
                    projectileType,
                    speed,
                    resolvedSpread,
                    hasGravity,
                    resolvedLifetime,
                    resolvedBlockLifetime
            );
        }
    }
}


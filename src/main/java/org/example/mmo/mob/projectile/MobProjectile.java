package org.example.mmo.mob.projectile;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal projectile contract mirroring the Atlas implementation.
 */
public interface MobProjectile {

    void launch(@NotNull Point from, double power, double spread);

    void launch(@NotNull Point from, @NotNull Point to, double power, double spread);
}

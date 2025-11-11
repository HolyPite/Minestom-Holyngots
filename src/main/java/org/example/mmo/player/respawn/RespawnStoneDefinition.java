package org.example.mmo.player.respawn;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.util.Objects;

/**
 * Immutable description of a respawn stone anchored to a block position.
 */
public record RespawnStoneDefinition(
        String id,
        int blockX,
        int blockY,
        int blockZ,
        Pos respawnPosition,
        Component displayName
) {

    public RespawnStoneDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(respawnPosition, "respawnPosition");
        Objects.requireNonNull(displayName, "displayName");
    }

    public boolean matches(Point block) {
        if (block == null) {
            return false;
        }
        return block.blockX() == blockX
                && block.blockY() == blockY
                && block.blockZ() == blockZ;
    }

    public Pos blockCenter() {
        return new Pos(blockX + 0.5, blockY + 0.5, blockZ + 0.5);
    }
}

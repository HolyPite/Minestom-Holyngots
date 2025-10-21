package org.example.mmo.quest.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A custom event fired when a player kills another entity.
 */
public class PlayerKillEntityEvent implements PlayerEvent {

    private final Player player;
    private final Entity killed;

    public PlayerKillEntityEvent(@NotNull Player player, @NotNull Entity killed) {
        this.player = player;
        this.killed = killed;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the entity that was killed.
     * @return The killed entity.
     */
    @NotNull
    public Entity getKilled() {
        return killed;
    }
}

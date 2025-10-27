package org.example.mmo.quest.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired whenever a player makes progress on a quest objective,
 * even if the objective is not yet complete (e.g., kill 1/5 mobs).
 */
public class QuestObjectiveProgressEvent implements PlayerEvent {

    private final Player player;

    public QuestObjectiveProgressEvent(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }
}

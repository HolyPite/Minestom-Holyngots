package org.example.mmo.quest.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

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

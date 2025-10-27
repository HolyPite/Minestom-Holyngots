package org.example.mmo.quest.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.example.mmo.quest.structure.Quest;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player fails a quest (e.g., time limit, too many attempts).
 */
public class QuestFailEvent implements PlayerEvent {

    private final Player player;
    private final Quest quest;

    public QuestFailEvent(@NotNull Player player, @NotNull Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Quest getQuest() {
        return quest;
    }
}

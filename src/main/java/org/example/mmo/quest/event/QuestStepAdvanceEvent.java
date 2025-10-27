package org.example.mmo.quest.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.example.mmo.quest.structure.Quest;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player advances to a new step in a quest, or completes it.
 */
public class QuestStepAdvanceEvent implements PlayerEvent {

    private final Player player;
    private final Quest quest;
    private final int newStepIndex; // Will be >= step count if the quest is completed

    public QuestStepAdvanceEvent(@NotNull Player player, @NotNull Quest quest, int newStepIndex) {
        this.player = player;
        this.quest = quest;
        this.newStepIndex = newStepIndex;
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

    public int getNewStepIndex() {
        return newStepIndex;
    }
}

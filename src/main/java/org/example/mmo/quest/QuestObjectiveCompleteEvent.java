package org.example.mmo.quest;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player completes a single quest objective.
 * This is used to check if a quest step can be advanced automatically.
 */
public class QuestObjectiveCompleteEvent implements PlayerEvent {

    private final Player player;
    private final QuestProgress questProgress;
    private final QuestStep completedStep;
    private final IQuestObjective completedObjective;

    public QuestObjectiveCompleteEvent(@NotNull Player player, @NotNull QuestProgress questProgress, @NotNull QuestStep completedStep, @NotNull IQuestObjective completedObjective) {
        this.player = player;
        this.questProgress = questProgress;
        this.completedStep = completedStep;
        this.completedObjective = completedObjective;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public QuestProgress getQuestProgress() {
        return questProgress;
    }

    @NotNull
    public QuestStep getCompletedStep() {
        return completedStep;
    }

    @NotNull
    public IQuestObjective getCompletedObjective() {
        return completedObjective;
    }
}

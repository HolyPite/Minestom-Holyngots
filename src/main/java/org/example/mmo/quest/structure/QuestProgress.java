package org.example.mmo.quest.structure;

import org.example.mmo.quest.api.IQuestObjective;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the progress of a single quest for a player.
 */
public class QuestProgress {
    public String questId;
    public int stepIndex;
    public long acceptedTime; // When the quest was first accepted
    public long stepStartTime; // When the current step was started, for duration checks
    public int attempts;
    // New: Tracks if an objective has been completed (even if step not advanced)
    // The key is a unique identifier for the objective (e.g., questId_stepIndex_objectiveIndex)
    public Map<String, Boolean> objectiveCompletionStatus = new HashMap<>();

    // Default constructor for deserialization
    public QuestProgress() {
    }

    public QuestProgress(String questId) {
        this.questId = questId;
        this.stepIndex = 0;
        this.acceptedTime = System.currentTimeMillis();
        this.stepStartTime = System.currentTimeMillis();
        this.attempts = 1;
    }

    // Helper methods for objective completion status
    public boolean isObjectiveCompleted(IQuestObjective objective) {
        // Use a more robust key for tracking objective completion status
        return objectiveCompletionStatus.getOrDefault(objective.getDescription().toString(), false);
    }

    public void setObjectiveCompleted(IQuestObjective objective, boolean completed) {
        objectiveCompletionStatus.put(objective.getDescription().toString(), completed);
    }

    public void resetObjectiveCompletionStatus() {
        objectiveCompletionStatus.clear();
    }
}

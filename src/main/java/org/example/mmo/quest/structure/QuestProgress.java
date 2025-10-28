package org.example.mmo.quest.structure;

import org.example.mmo.quest.api.IQuestObjective;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    public String questId;
    public int stepIndex;
    public long acceptedTime;
    public long stepStartTime;
    public int attempts;
    public Map<String, Boolean> objectiveCompletionStatus = new HashMap<>();

    public QuestProgress() {}

    public QuestProgress(String questId) {
        this.questId = questId;
        this.stepIndex = 0;
        this.acceptedTime = System.currentTimeMillis();
        this.stepStartTime = System.currentTimeMillis();
        this.attempts = 1;
    }

    public boolean isObjectiveCompleted(IQuestObjective objective) {
        return objectiveCompletionStatus.getOrDefault(objective.getDescription().toString(), false);
    }

    public void setObjectiveCompleted(IQuestObjective objective, boolean completed) {
        objectiveCompletionStatus.put(objective.getDescription().toString(), completed);
    }

    public void resetObjectiveCompletionStatus() {
        objectiveCompletionStatus.clear();
    }
}

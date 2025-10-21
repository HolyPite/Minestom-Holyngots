package org.example.mmo.quest;

/**
 * Stores the progress of a single quest for a player.
 */
public class QuestProgress {
    public String questId;
    public int stepIndex;
    public long acceptedTime; // When the quest was first accepted
    public long stepStartTime; // When the current step was started, for duration checks
    public int attempts;

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
}

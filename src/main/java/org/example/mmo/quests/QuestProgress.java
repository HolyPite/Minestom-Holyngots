package org.example.mmo.quests;

/**
 * Stocke l'état d'avancement d'une quête pour un joueur.
 */
public class QuestProgress {
    public String questId;
    public int stepIndex;
    public long acceptedTime;
    public int attempts;

    public QuestProgress() {
    }

    public QuestProgress(String questId) {
        this.questId = questId;
        this.stepIndex = 0;
        this.acceptedTime = System.currentTimeMillis();
    }
}

package org.example.data.data_class;

import net.minestom.server.coordinate.Pos;
import org.example.mmo.quest.structure.QuestProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's persistent information.
 */
public class PlayerData {
    public UUID uuid;
    public int level;
    /**
     * Experience accumulated toward the next level.
     */
    public int experience;
    public List<ItemData> inventory = new ArrayList<>();
    public List<QuestProgress> quests = new ArrayList<>();
    public List<String> completedQuests = new ArrayList<>();
    public List<String> failedQuests = new ArrayList<>();
    public Pos position;
    public String lastInstance;

    public Map<String, Integer> questCounters = new HashMap<>();
    /**
     * Stores the completion timestamp for repeatable quests to manage cooldowns.
     * Key: Quest ID, Value: System.currentTimeMillis() of completion.
     */
    public Map<String, Long> questCooldowns = new HashMap<>();

    public PlayerData() {}

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
    }

    // --- Quest Helpers ---

    public Integer getQuestCounter(String key) {
        return questCounters.getOrDefault(key, 0);
    }

    public void setQuestCounter(String key, int value) {
        questCounters.put(key, value);
    }

    public void incrementQuestCounter(String key, int amount) {
        questCounters.merge(key, amount, Integer::sum);
    }

    public boolean hasCompletedQuest(String questId) {
        return completedQuests.contains(questId);
    }

    public boolean hasFailedQuest(String questId) {
        return failedQuests.contains(questId);
    }

    public boolean hasReachedQuestStep(String questId, int stepNumber) {
        if (hasCompletedQuest(questId)) {
            return true;
        }

        if (stepNumber <= 0) {
            return hasCompletedQuest(questId);
        }

        int targetIndex = stepNumber - 1;
        return quests.stream()
                .filter(p -> p.questId.equals(questId))
                .anyMatch(p -> p.stepIndex >= targetIndex);
    }
}

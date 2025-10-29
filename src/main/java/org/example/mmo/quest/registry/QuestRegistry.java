package org.example.mmo.quest.registry;

import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.*;

public final class QuestRegistry {
    private static final Map<String, Quest> MAP = new HashMap<>();
    private static final Map<String, List<Quest>> QUESTS_BY_START_NPC = new HashMap<>();
    private static final List<Quest> AUTO_START_QUESTS = new ArrayList<>();
    private static final Map<String, Quest> READ_ONLY_MAP = Collections.unmodifiableMap(MAP);
    private static final List<Quest> READ_ONLY_AUTO_START = Collections.unmodifiableList(AUTO_START_QUESTS);

    public static void register(Quest quest) {
        Quest previous = MAP.put(quest.id, quest);
        if (previous != null) {
            removeFromIndexes(previous);
        }
        System.out.println("[QuestRegistry] +" + quest.id);

        if (quest.steps.isEmpty()) {
            return;
        }

        QuestStep firstStep = quest.steps.getFirst();
        String startNpc = firstStep.startNpc;

        if (startNpc == null || startNpc.isBlank()) {
            AUTO_START_QUESTS.add(quest);
        } else {
            QUESTS_BY_START_NPC.computeIfAbsent(startNpc, key -> new ArrayList<>()).add(quest);
        }
    }

    public static Quest byId(String id) {
        return MAP.get(id);
    }

    public static Map<String, Quest> all() {
        return READ_ONLY_MAP;
    }

    public static List<Quest> byStartNpc(String npcId) {
        List<Quest> quests = QUESTS_BY_START_NPC.get(npcId);
        if (quests == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(quests);
    }

    public static List<Quest> autoStartQuests() {
        return READ_ONLY_AUTO_START;
    }

    private static void removeFromIndexes(Quest quest) {
        if (quest.steps.isEmpty()) {
            return;
        }

        QuestStep firstStep = quest.steps.getFirst();
        String startNpc = firstStep.startNpc;

        if (startNpc == null || startNpc.isBlank()) {
            AUTO_START_QUESTS.remove(quest);
        } else {
            List<Quest> quests = QUESTS_BY_START_NPC.get(startNpc);
            if (quests != null) {
                quests.remove(quest);
                if (quests.isEmpty()) {
                    QUESTS_BY_START_NPC.remove(startNpc);
                }
            }
        }
    }
}

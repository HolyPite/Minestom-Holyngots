package org.example.mmo.quest.registry;

import org.example.mmo.quest.structure.Quest;

import java.util.HashMap;
import java.util.Map;

public final class QuestRegistry {
    private static final Map<String, Quest> MAP = new HashMap<>();

    public static void register(Quest quest) {
        MAP.put(quest.id, quest);
        System.out.println("[QuestRegistry] +" + quest.id);
    }

    public static Quest byId(String id) {
        return MAP.get(id);
    }

    public static Map<String, Quest> all() {
        return Map.copyOf(MAP);
    }
}

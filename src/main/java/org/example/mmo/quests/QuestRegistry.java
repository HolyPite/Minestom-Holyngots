package org.example.mmo.quests;

import java.util.HashMap;
import java.util.Map;

/**
 * Registre contenant toutes les quêtes connues du serveur.
 */
public final class QuestRegistry {
    private static final Map<String, Quest> MAP = new HashMap<>();

    private QuestRegistry() {}

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

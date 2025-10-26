package org.example.mmo.npc;

import java.util.HashMap;
import java.util.Map;

public final class NpcRegistry {
    private static final Map<String, NPC> MAP = new HashMap<>();

    public static void register(NPC npc) {
        MAP.put(npc.id(), npc);
        System.out.println("[NpcRegistry] +" + npc.id());
    }

    public static NPC byId(String id) {
        return MAP.get(id);
    }

    public static Map<String, NPC> all() {
        return Map.copyOf(MAP);
    }
}

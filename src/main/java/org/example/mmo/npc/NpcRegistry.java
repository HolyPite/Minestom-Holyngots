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
        if (id == null) {
            return null;
        }

        NPC npc = MAP.get(id);
        if (npc == null) {
            System.err.println("[NpcRegistry] Unknown NPC id requested: " + id);
        }
        return npc;
    }

    public static Map<String, NPC> all() {
        return Map.copyOf(MAP);
    }
}

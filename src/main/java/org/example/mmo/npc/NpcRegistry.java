package org.example.mmo.npc;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NpcRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcRegistry.class);
    private static final Map<String, NPC> MAP = new HashMap<>();

    private NpcRegistry() {
    }

    public static void register(NPC npc) {
        MAP.put(npc.id(), npc);
        LOGGER.info("Registered NPC {}", npc.id());
    }

    public static NPC byId(String id) {
        if (id == null) {
            return null;
        }

        NPC npc = MAP.get(id);
        if (npc == null) {
            LOGGER.warn("Unknown NPC id requested: {}", id);
        }
        return npc;
    }

    public static Map<String, NPC> all() {
        return Map.copyOf(MAP);
    }
}

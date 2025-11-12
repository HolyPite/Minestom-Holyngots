package org.example.mmo.item;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ItemRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRegistry.class);
    private static final Map<String, GameItem> MAP = new HashMap<>();

    private ItemRegistry() {
    }

    public static void register(GameItem item) {
        MAP.put(item.id, item);
        LOGGER.info("Registered item {}", item.id);
    }

    public static GameItem byId(String id) {
        return MAP.get(id);
    }

    public static Map<String, GameItem> all() {
        return Map.copyOf(MAP);
    }
}

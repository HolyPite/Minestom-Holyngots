package org.example.items;

import java.util.HashMap;
import java.util.Map;

public final class ItemRegistry {
    private static final Map<String, GameItem> MAP = new HashMap<>();

    public static void register(GameItem item) {
        MAP.put(item.id, item);
        System.out.println("[ItemRegistry] +" + item.id);
    }

    public static GameItem byId(String id)     { return MAP.get(id); }

    public static Map<String, GameItem> all()  { return Map.copyOf(MAP); }
}

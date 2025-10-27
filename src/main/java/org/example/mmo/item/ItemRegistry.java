package org.example.mmo.item;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<String, GameItem> idToItem = new HashMap<>();
    private static final Map<String, Integer> idToCustomModelData = new HashMap<>();
    private static final Map<Integer, String> customModelDataToId = new HashMap<>();
    private static int nextCustomModelData = 1;

    public static void register(GameItem item) {
        idToItem.put(item.id, item);
        if (!idToCustomModelData.containsKey(item.id)) {
            idToCustomModelData.put(item.id, nextCustomModelData);
            customModelDataToId.put(nextCustomModelData, item.id);
            nextCustomModelData++;
        }
        System.out.println("[ItemRegistry] +" + item.id);
    }

    public static GameItem byId(String id) {
        return idToItem.get(id);
    }

    public static int getCustomModelData(String id) {
        return idToCustomModelData.getOrDefault(id, 0);
    }

    public static GameItem fromCustomModelData(int customModelData) {
        String id = customModelDataToId.get(customModelData);
        return (id != null) ? byId(id) : null;
    }
}

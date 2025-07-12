package org.example.data.data_class;

import java.util.*;
import java.util.UUID;

/**
 * Simple representation of a player's persistent information.
 */
public class PlayerData {
    public UUID uuid;
    public int level;
    public int experience;
    /** Inventories per instance group. */
    public Map<String, List<ItemData>> inventories = new HashMap<>();

    /** Legacy single inventory for backward compatibility. */
    public List<ItemData> inventory = new ArrayList<>();

    public PlayerData() {}

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
    }
}

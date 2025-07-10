package org.example.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple representation of a player's persistent information.
 */
public class PlayerData {
    public UUID uuid;
    public int level;
    public int experience;
    public List<ItemData> inventory = new ArrayList<>();

    public PlayerData() {}

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
    }
}

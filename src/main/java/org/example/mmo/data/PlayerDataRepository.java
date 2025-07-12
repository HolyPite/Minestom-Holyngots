package org.example.mmo.data;

import org.example.mmo.data.data_class.PlayerData;

import java.util.UUID;

/**
 * Abstraction for loading and saving persistent player data.
 */
public interface PlayerDataRepository {
    PlayerData load(UUID playerId);
    void save(PlayerData data);
}

package org.example.data;

import java.util.UUID;

/**
 * Abstraction for loading and saving persistent player data.
 */
public interface PlayerDataRepository {
    PlayerData load(UUID playerId);
    void save(PlayerData data);
}

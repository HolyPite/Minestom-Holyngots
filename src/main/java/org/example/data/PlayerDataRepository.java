package org.example.data;

import net.minestom.server.instance.Instance;
import org.example.data.data_class.PlayerData;

import java.util.Set;
import java.util.UUID;

/**
 * Abstraction for loading and saving persistent player data.
 */
public interface PlayerDataRepository {
    PlayerData load(UUID playerId, Set<Instance> instanceType);
    void save(PlayerData data, Set<Instance> instanceType);
}


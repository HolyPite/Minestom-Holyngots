package org.example.mmo.player.data;

import net.minestom.server.instance.Instance;
import org.example.data.data_class.PlayerData;

import java.util.Set;
import java.util.UUID;

public interface PlayerDataRepository {
    PlayerData load(UUID playerId, Set<Instance> instanceType);
    void save(PlayerData data, Set<Instance> instanceType);
}

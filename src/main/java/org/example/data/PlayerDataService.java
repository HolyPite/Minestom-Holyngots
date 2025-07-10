package org.example.data;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles loading and saving player data when players connect or disconnect.
 */
public class PlayerDataService {
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataService(PlayerDataRepository repository) {
        this.repository = repository;
    }

    public void init(GlobalEventHandler events) {
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = repository.load(player.getUuid());
            cache.put(player.getUuid(), data);
        });

        events.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = cache.remove(player.getUuid());
            if (data != null) {
                repository.save(data);
            }
        });
    }

    public PlayerData get(Player player) {
        return cache.get(player.getUuid());
    }
}

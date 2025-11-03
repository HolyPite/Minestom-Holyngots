package org.example.bootstrap;

import java.util.Objects;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.InventoryEvent;
import org.example.mmo.player.data.PlayerDataService;
import org.example.mmo.npc.mob.MobSpawnService;

/**
 * Holds shared singletons that represent the running game context.
 * The context is initialised once during bootstrap and queried by the gameplay code.
 */
public final class GameContext {

    private static volatile GameContext instance;

    private final InstanceRegistry instanceRegistry;
    private final GameLifecycle lifecycle;

    private GameContext(InstanceRegistry instanceRegistry, GameLifecycle lifecycle) {
        this.instanceRegistry = instanceRegistry;
        this.lifecycle = lifecycle;
    }

    public static void initialise(InstanceRegistry instances, GameLifecycle lifecycle) {
        instance = new GameContext(instances, lifecycle);
    }

    public static GameContext get() {
        return Objects.requireNonNull(instance, "GameContext has not been initialised yet");
    }

    public InstanceRegistry instances() {
        return instanceRegistry;
    }

    public GameLifecycle lifecycle() {
        return lifecycle;
    }

    public PlayerDataService playerDataService() {
        return lifecycle.playerDataService();
    }

    public EventNode<Event> gameNode() {
        return lifecycle.gameNode();
    }

    public EventNode<PlayerEvent> playerNode() {
        return lifecycle.playerNode();
    }

    public EventNode<InventoryEvent> inventoryNode() {
        return lifecycle.inventoryNode();
    }

    public MobSpawnService mobSpawnService() {
        return lifecycle.mobSpawnService();
    }
}

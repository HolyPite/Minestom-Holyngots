package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;
import org.example.data.teleport.TeleportUtils;
import org.example.mmo.item.datas.Stats;

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();

        // Initialize instances and all game systems
        InstancesInit.init();
        NodesManagement.init();

        // --- Phase 1: Player Configuration ---
        GLOBAL_EVENTS.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();

            // --- DIAGNOSTIC TEST ---
            // We are temporarily disabling the call to TeleportUtils to confirm it is the source of the crash.
            // We will just spawn the player in the first available game instance.
            if (!InstancesInit.GAME_INSTANCES.isEmpty()) {
                // Correct way to get the first element from a generic collection
                event.setSpawningInstance(InstancesInit.GAME_INSTANCES.iterator().next());
            }
            // --- END OF DIAGNOSTIC TEST ---

            // Original code that is likely causing the issue:
            // TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player, InstancesInit.GAME_INSTANCES);
            // event.setSpawningInstance(target.instance());
        });

        // --- Phase 2: Player Spawning ---
        GLOBAL_EVENTS.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();

            // Now that the player is in-game, we can safely load their data and teleport them.
            PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), InstancesInit.GAME_INSTANCES);
            TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player, InstancesInit.GAME_INSTANCES);

            // Teleport the player to their actual last position
            player.teleport(target.pos());
            player.setRespawnPoint(target.pos());

            // Refresh player stats for the first time now that they are fully in-game
            Stats.refresh(player);
        });

        // World saving
        InstancesSaving.init();

        // Start the server
        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }
}

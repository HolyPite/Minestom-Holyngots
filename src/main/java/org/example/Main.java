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
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.registry.QuestRegistry;

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
            if (!InstancesInit.GAME_INSTANCES.isEmpty()) {
                event.setSpawningInstance(InstancesInit.GAME_INSTANCES.iterator().next());
            }
        });

        // --- Phase 2: Player Spawning ---
        GLOBAL_EVENTS.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();

            // Load data into the service
            PlayerDataUtils.loadLastData(player.getUuid(), InstancesInit.GAME_INSTANCES);

            // Get the official data object from the service
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null) return; // Safety check

            // Teleport the player to their actual last position
            TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player, InstancesInit.GAME_INSTANCES);
            player.teleport(target.pos());
            player.setRespawnPoint(target.pos());

            // Refresh stats
            Stats.refresh(player);

            // Try to auto-start quests using the official data object
            QuestRegistry.all().values().forEach(quest -> {
                QuestManager.tryAutoStartQuest(player, data, quest);
            });
        });

        // World saving
        InstancesSaving.init();

        // Start the server
        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }
}

package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;
import org.example.data.teleport.TeleportUtils;
import org.example.mmo.dev.QuestEntitySpawner;

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();

        // Initialize instances and all game systems (including quests and combat)
        InstancesInit.init();
        NodesManagement.init();

        // Player spawn configuration
        GLOBAL_EVENTS.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();

            // Your custom data loading and teleportation logic
            PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), InstancesInit.GAME_INSTANCES);
            TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player, InstancesInit.GAME_INSTANCES);

            event.setSpawningInstance(target.instance());
            player.setRespawnPoint(target.pos());

            // Spawn NPCs for testing quests
            QuestEntitySpawner.spawnQuestNpcs(target.instance(), target.pos());
        });

        // World saving
        InstancesSaving.init();

        // Start the server
        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }
}

package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
//import net.minestom.server.extras.MojangAuth;
import org.example.bootstrap.GameContext;
import org.example.bootstrap.GameLifecycle;
import org.example.bootstrap.InstanceBootstrap;
import org.example.bootstrap.InstanceRegistry;
import org.example.bootstrap.InstancesSaving;
import org.example.bootstrap.motd.MotdService;
import org.example.mmo.player.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;
import org.example.mmo.player.teleport.TeleportUtils;
import org.example.mmo.item.datas.Stats;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Main {
    private static final Set<UUID> connectedPlayers = new HashSet<>();

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();

        // Bootstrap the world instances and game systems
        InstanceRegistry instanceRegistry = new InstanceBootstrap().createRegistry();
        GameLifecycle lifecycle = new GameLifecycle(instanceRegistry);
        GameContext.initialise(instanceRegistry, lifecycle);
        MotdService.register(GLOBAL_EVENTS);

        // --- Phase 1: Player Configuration ---
        GLOBAL_EVENTS.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            if (!instanceRegistry.gameInstances().isEmpty()) {
                event.setSpawningInstance(instanceRegistry.gameInstances().iterator().next());
            }
        });

        // --- Phase 2: Player Spawning ---
        GLOBAL_EVENTS.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();

            // Check if this is the first spawn for this session
            if (!connectedPlayers.contains(player.getUuid())) {
                connectedPlayers.add(player.getUuid());

                // --- General Logic (runs for any instance group) ---
                PlayerDataUtils.loadLastData(player.getUuid(), instanceRegistry.gameInstances());
                PlayerData data = GameContext.get().playerDataService().get(player);
                if (data == null) return;

                TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player, instanceRegistry.gameInstances());
                player.teleport(target.pos());
                player.setRespawnPoint(target.pos());

                // --- Game-Specific Logic ---
                if (instanceRegistry.isGameInstance(event.getSpawnInstance())) {
                    Stats.refresh(player);

                    for (QuestProgress progress : data.quests) {
                        Quest quest = QuestRegistry.byId(progress.questId);
                        if (quest != null && progress.stepIndex < quest.steps.size()) {
                            QuestStep currentStep = quest.steps.get(progress.stepIndex);
                            for (var objective : currentStep.objectives) {
                                if (objective instanceof LocationObjective && !progress.isObjectiveCompleted(objective)) {
                                    objective.onStart(player, data);
                                }
                            }
                        }
                    }

                    QuestRegistry.autoStartQuests().forEach(quest -> {
                        QuestManager.tryAutoStartQuest(player, data, quest);
                    });
                }
            }
        });

        // --- Player Disconnect ---
        GLOBAL_EVENTS.addListener(PlayerDisconnectEvent.class, event -> {
            connectedPlayers.remove(event.getPlayer().getUuid());
        });

        // World saving
        InstancesSaving.init();

        // Start the server
        //MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }
}

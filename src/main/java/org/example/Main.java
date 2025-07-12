package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();


        //instance declaration
        InstancesInit.init();

        //NodeInit
        NodesManagement.init();


        // Gestion du spawn du joueur
        GLOBAL_EVENTS.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(InstancesInit.GAME_INSTANCE_1);
            player.setRespawnPoint(new Pos(0, 42, 0));

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(InstancesInit.GAME_INSTANCE_1,new Pos(0, 42, 0));

        });



        var scheduler = MinecraftServer.getSchedulerManager();

        //Save worlds
        scheduler.buildShutdownTask( () -> {
            System.out.println("Server shutting down... Saving chunk");
            MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
        });

        scheduler.buildTask( () -> {
                    System.out.println("Server autosave... Saving chunk");
                    MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
                })      .repeat(Duration.ofSeconds(30))
                .delay(Duration.ofMinutes(1))
                .schedule();


        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }

}
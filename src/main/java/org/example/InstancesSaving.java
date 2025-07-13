package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;

import java.time.Duration;

public class InstancesSaving {

    public static void init(){
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
    }
}

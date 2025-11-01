package org.example.bootstrap;

import java.time.Duration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralises the automatic and shutdown world-saving tasks so they live alongside the
 * other bootstrap wiring.
 */
public final class InstancesSaving {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstancesSaving.class);

    private InstancesSaving() {
    }

    public static void init() {
        var scheduler = MinecraftServer.getSchedulerManager();

        scheduler.buildShutdownTask(() -> {
            LOGGER.info("Server shutting down, saving chunks");
            MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
        });

        scheduler.buildTask(() -> {
                    LOGGER.info("Automatic world save in progress");
                    MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
                })
                .repeat(Duration.ofSeconds(30))
                .delay(Duration.ofMinutes(1))
                .schedule();
    }
}

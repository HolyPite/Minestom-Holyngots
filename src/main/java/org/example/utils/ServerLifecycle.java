package org.example.utils;

import net.minestom.server.MinecraftServer;

/** Utility methods for controlling the server lifecycle. */
public final class ServerLifecycle {
    private ServerLifecycle() {}

    /**
     * Shuts down the Minecraft server cleanly by invoking
     * {@link MinecraftServer#stopCleanly()}.
     */
    public static void shutdown() {
        MinecraftServer.stopCleanly();
    }
}

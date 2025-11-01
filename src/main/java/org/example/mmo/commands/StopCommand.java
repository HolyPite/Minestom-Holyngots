package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import org.example.utils.ServerLifecycle;

/**
 * Command to stop the Minecraft server cleanly.
 */
public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "shutdown");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Server shutting down...");
            ServerLifecycle.shutdown();
        });
    }
}

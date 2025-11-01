package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import org.example.bootstrap.GameContext;

import static org.example.mmo.player.teleport.TeleportUtils.teleportToInstance;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("tpworld");

        var worldArg = ArgumentType.String("world");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /tpworld <game1|game2|build1>"));

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }

            String worldName = context.get(worldArg);
            InstanceContainer target = GameContext.get().instances().byName(worldName);
            if (target == null) {
                sender.sendMessage("Unknown world: " + worldName);
                return;
            }

            if (target == player.getInstance()) {
                sender.sendMessage("Already in " + worldName);
                return;
            }

            teleportToInstance(player, target);
            sender.sendMessage("Teleported to " + worldName);
        }, worldArg);
    }
}

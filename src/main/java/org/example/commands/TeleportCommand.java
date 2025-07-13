package org.example.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.InstancesInit;
import net.minestom.server.instance.InstanceContainer;

import static org.example.data.PlayerDataUtils.loadLastData;
import static org.example.teleport.TeleportUtils.teleportToInstance;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("tpworld");

        var worldArg = ArgumentType.String("world");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /tpworld <game1|game2|build1>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }

            String worldName = context.get(worldArg);
            InstanceContainer target = InstancesInit.instance_get(worldName);
            if (target == null) {
                sender.sendMessage("Unknown world: " + worldName);
                return;
            }

            if (target == player.getInstance()){
                sender.sendMessage("Already in " + worldName);
                return;
            }

            teleportToInstance(player,target);
            sender.sendMessage("Teleported to " + worldName);
        }, worldArg);
    }
}

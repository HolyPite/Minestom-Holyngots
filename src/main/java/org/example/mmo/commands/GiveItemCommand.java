package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.mmo.item.ItemRegistry;

public final class GiveItemCommand extends Command {

    public GiveItemCommand() {
        super("giveitem", "item", "gi");

        var idArg = ArgumentType.String("id");

        setDefaultExecutor((sender, ctx) -> sender.sendMessage("Usage: /giveitem <id>"));

        addSyntax((sender, ctx) -> {
            String id = ctx.get(idArg);
            var item = ItemRegistry.byId(id);

            if (item == null) {
                sender.sendMessage("Unknown item id: " + id);
                return;
            }
            if (sender instanceof Player player) {
                player.getInventory().addItemStack(item.toItemStack());
                sender.sendMessage("Received 1x " + item.id);
            }

        }, idArg);
    }
}

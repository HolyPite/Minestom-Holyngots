package org.example.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.mmo.item.ItemRegistry;

public final class GiveItemCommand extends Command {

    public GiveItemCommand() {
        super("giveitem", "item", "gi");

        var idArg = ArgumentType.String("id");

        setDefaultExecutor((sender, ctx) ->
                sender.sendMessage("§cUsage : /giveitem <id>"));

        addSyntax((sender, ctx) -> {

            String id = ctx.get(idArg);
            var item = ItemRegistry.byId(id);

            if (item == null) {
                sender.sendMessage("§cInconnu : " + id);
                return;
            }
            if (sender instanceof Player player) {
                player.getInventory().addItemStack(item.toItemStack());
                sender.sendMessage("§aVous recevez ×1 " + item.id);
            }

        }, idArg);
    }
}

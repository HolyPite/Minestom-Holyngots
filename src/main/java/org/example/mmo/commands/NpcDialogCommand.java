package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.mmo.npc.dialog.NpcDialogService;

public final class NpcDialogCommand extends Command {

    public NpcDialogCommand() {
        super("npcdialog");

        setCondition((sender, context) -> sender instanceof Player);

        var sessionArg = ArgumentType.Long("session");
        var actionArg = ArgumentType.String("action");

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            long sessionId = context.get(sessionArg);
            String actionKey = context.get(actionArg);
            NpcDialogService.executeAction(player, sessionId, actionKey);
        }, sessionArg, actionArg);
    }
}

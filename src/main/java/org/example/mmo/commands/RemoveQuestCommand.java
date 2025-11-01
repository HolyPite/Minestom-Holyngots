package org.example.mmo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;

public class RemoveQuestCommand extends Command {

    public RemoveQuestCommand() {
        super("removequest");

        var questIdArg = ArgumentType.String("questId");
        var playerArg = ArgumentType.String("player");

        playerArg.setSuggestionCallback((sender, context, suggestion) ->
                MinecraftServer.getConnectionManager().getOnlinePlayers()
                        .forEach(p -> suggestion.addEntry(new SuggestionEntry(p.getUsername()))));
        playerArg.isOptional();

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /removequest <questId|all> [player]"));

        addSyntax((sender, context) -> {
            Player target;
            String playerName = context.get(playerArg);
            if (playerName != null) {
                target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("You must specify a player when running from the console.");
                return;
            }

            String questId = context.get(questIdArg);
            PlayerData data = GameContext.get().playerDataService().get(target);
            if (data == null) {
                sender.sendMessage(Component.text("Player data not loaded.", NamedTextColor.RED));
                return;
            }

            if ("all".equalsIgnoreCase(questId)) {
                data.quests.clear();
                data.completedQuests.clear();
                data.failedQuests.clear();
                data.questCooldowns.clear();
                data.questCounters.clear();
                sender.sendMessage(Component.text("All quests removed for player " + target.getUsername() + ".", NamedTextColor.GREEN));
            } else {
                boolean removedInProgress = data.quests.removeIf(p -> p.questId.equals(questId));
                boolean removedCompleted = data.completedQuests.remove(questId);
                boolean removedFailed = data.failedQuests.remove(questId);
                boolean removedCooldown = data.questCooldowns.remove(questId) != null;

                if (removedInProgress || removedCompleted || removedFailed || removedCooldown) {
                    sender.sendMessage(Component.text("Quest '" + questId + "' removed for player " + target.getUsername() + ".", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Player " + target.getUsername() + " did not have quest '" + questId + "'.", NamedTextColor.YELLOW));
                }
            }
        }, questIdArg, playerArg);
    }
}

package org.example.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;

/**
 * Admin command to remove quests from a player.
 * Usage: /removequest <questId|all> [player]
 */
public class RemoveQuestCommand extends Command {

    public RemoveQuestCommand() {
        super("removequest");

        // setCondition(sender -> sender.hasPermission("command.removequest"));

        var questIdArg = ArgumentType.String("questId");
        var playerArg = ArgumentType.String("player");

        // Set up dynamic suggestions for player names
        playerArg.setSuggestionCallback(
            (sender, context, suggestion) -> {
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                suggestion.addEntry(new SuggestionEntry(onlinePlayer.getUsername()));
            }
        });

        playerArg.isOptional();

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /removequest <questId|all> [player]");
        });

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
            PlayerData data = NodesManagement.getDataService().get(target);
            if (data == null) {
                sender.sendMessage(Component.text("Player data not loaded.", NamedTextColor.RED));
                return;
            }

            if (questId.equalsIgnoreCase("all")) {
                // Remove all quest data
                data.quests.clear();
                data.completedQuests.clear();
                data.failedQuests.clear();
                data.questCooldowns.clear();
                data.questCounters.clear();
                sender.sendMessage(Component.text("All quests removed for player " + target.getUsername() + ".", NamedTextColor.GREEN));
            } else {
                // Remove a specific quest
                boolean removedInProgress = data.quests.removeIf(p -> p.questId.equals(questId));
                boolean removedCompleted = data.completedQuests.remove(questId);
                boolean removedFailed = data.failedQuests.remove(questId);
                boolean removedCooldown = data.questCooldowns.remove(questId) != null;

                if (removedInProgress || removedCompleted || removedFailed || removedCooldown) {
                    sender.sendMessage(Component.text("Quest '" + questId + "' removed from all records for player " + target.getUsername() + ".", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Player " + target.getUsername() + " did not have quest '" + questId + "' in their records.", NamedTextColor.YELLOW));
                }
            }

        }, questIdArg, playerArg);
    }
}

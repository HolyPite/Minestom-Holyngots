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
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;

import java.util.Optional;

/**
 * Admin command to set a player's progress in a quest.
 * Usage: /setquest <questId> <stepIndex> [player]
 */
public class SetQuestCommand extends Command {

    public SetQuestCommand() {
        super("setquest");

        // setCondition(sender -> sender.hasPermission("command.setquest"));

        var questIdArg = ArgumentType.String("questId");
        var stepArg = ArgumentType.Integer("stepIndex").min(1);
        var playerArg = ArgumentType.String("player");

        // Set up dynamic suggestions for player names
        playerArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                suggestion.addEntry(new SuggestionEntry(onlinePlayer.getUsername()));
            }
        });

        playerArg.isOptional();

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /setquest <questId> <stepIndex> [player]");
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
            int stepIndex = context.get(stepArg) - 1; // Convert from 1-based to 0-based index

            Quest quest = QuestRegistry.byId(questId);
            if (quest == null) {
                sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
                return;
            }

            if (stepIndex >= quest.steps.size()) {
                sender.sendMessage(Component.text("Invalid step index. This quest only has " + quest.steps.size() + " steps.", NamedTextColor.RED));
                return;
            }

            PlayerData data = NodesManagement.getDataService().get(target);
            if (data == null) {
                sender.sendMessage(Component.text("Player data not loaded.", NamedTextColor.RED));
                return;
            }

            Optional<QuestProgress> progressOpt = data.quests.stream().filter(p -> p.questId.equals(questId)).findFirst();
            QuestProgress progress;
            if (progressOpt.isPresent()) {
                progress = progressOpt.get();
            } else {
                progress = new QuestProgress(questId);
                data.quests.add(progress);
            }

            progress.stepIndex = stepIndex;
            progress.stepStartTime = System.currentTimeMillis();
            progress.attempts = 0;

            quest.steps.get(stepIndex).objectives.forEach(obj -> obj.onStart(target, data));

            sender.sendMessage(Component.text("Set quest '" + questId + "' to step " + (stepIndex + 1) + " for player " + target.getUsername() + ".", NamedTextColor.GREEN));

        }, questIdArg, stepArg, playerArg);
    }
}

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
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;

import java.util.Optional;

public class SetQuestCommand extends Command {

    public SetQuestCommand() {
        super("setquest");

        var questIdArg = ArgumentType.String("questId");
        var stepArg = ArgumentType.Integer("stepIndex").min(1);
        var playerArg = ArgumentType.String("player");

        playerArg.setSuggestionCallback((sender, context, suggestion) ->
                MinecraftServer.getConnectionManager().getOnlinePlayers()
                        .forEach(p -> suggestion.addEntry(new SuggestionEntry(p.getUsername()))));
        playerArg.isOptional();

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /setquest <questId> <stepIndex> [player]"));

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
            int stepIndex = context.get(stepArg) - 1;

            Quest quest = QuestRegistry.byId(questId);
            if (quest == null) {
                sender.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
                return;
            }

            if (stepIndex >= quest.steps.size()) {
                sender.sendMessage(Component.text("Invalid step index. This quest only has " + quest.steps.size() + " steps.", NamedTextColor.RED));
                return;
            }

            PlayerData data = GameContext.get().playerDataService().get(target);
            if (data == null) {
                sender.sendMessage(Component.text("Player data not loaded.", NamedTextColor.RED));
                return;
            }

            Optional<QuestProgress> progressOpt = data.quests.stream()
                    .filter(p -> p.questId.equals(questId))
                    .findFirst();

            QuestProgress progress = progressOpt.orElseGet(() -> {
                QuestProgress newProgress = new QuestProgress(questId);
                data.quests.add(newProgress);
                return newProgress;
            });

            progress.stepIndex = stepIndex;
            progress.stepStartTime = System.currentTimeMillis();
            progress.attempts = 0;

            quest.steps.get(stepIndex).objectives.forEach(obj -> obj.onStart(target, data));

            sender.sendMessage(Component.text("Set quest '" + questId + "' to step " + (stepIndex + 1) + " for player " + target.getUsername() + ".", NamedTextColor.GREEN));
        }, questIdArg, stepArg, playerArg);
    }
}

package org.example.mmo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.SlayObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.utils.TKit;

public class QuestsCommand extends Command {

    public QuestsCommand() {
        super("quests");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can check their quests.");
                return;
            }
            PlayerData data = GameContext.get().playerDataService().get(player);
            if (data == null) {
                return;
            }

            player.sendMessage(Component.text("--- Journal de quetes ---", NamedTextColor.GOLD));
            player.sendMessage(Component.text("Quetes en cours :", NamedTextColor.YELLOW));

            if (data.quests.isEmpty()) {
                player.sendMessage(Component.text("  Aucune", NamedTextColor.GRAY));
            } else {
                for (QuestProgress progress : data.quests) {
                    Quest quest = QuestRegistry.byId(progress.questId);
                    if (quest != null) {
                        player.sendMessage(Component.text("  - " + TKit.extractPlainText(quest.name) + " (Étape " + (progress.stepIndex + 1) + ")", NamedTextColor.WHITE));
                    }
                }
            }

            player.sendMessage(Component.text("Quetes terminees :", NamedTextColor.YELLOW));
            if (data.completedQuests.isEmpty()) {
                player.sendMessage(Component.text("  Aucune", NamedTextColor.GRAY));
            } else {
                for (String questId : data.completedQuests) {
                    Quest quest = QuestRegistry.byId(questId);
                    if (quest != null) {
                        player.sendMessage(Component.text("  - " + TKit.extractPlainText(quest.name), NamedTextColor.GREEN));
                    }
                }
            }
        });

        var questIdArg = ArgumentType.String("questId");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }
            PlayerData data = GameContext.get().playerDataService().get(player);
            if (data == null) {
                return;
            }

            String questId = context.get(questIdArg);
            Quest quest = QuestRegistry.byId(questId);
            if (quest == null) {
                player.sendMessage(Component.text("Quete inconnue : " + questId, NamedTextColor.RED));
                return;
            }

            QuestProgress progress = data.quests.stream()
                    .filter(p -> p.questId.equals(questId))
                    .findFirst()
                    .orElse(null);

            player.sendMessage(Component.text("--- " + TKit.extractPlainText(quest.name) + " ---", NamedTextColor.GOLD));
            player.sendMessage(quest.description.color(NamedTextColor.GRAY));

            if (progress == null) {
                if (data.hasCompletedQuest(questId)) {
                    player.sendMessage(Component.text("Statut : Termine", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Statut : Non commencee", NamedTextColor.GRAY));
                }
                return;
            }

            if (progress.stepIndex >= quest.steps.size()) {
                return;
            }

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            player.sendMessage(Component.text("Étape actuelle : " + TKit.extractPlainText(currentStep.name), NamedTextColor.YELLOW));
            player.sendMessage(currentStep.description.color(NamedTextColor.WHITE));

            player.sendMessage(Component.text("Objectifs :", NamedTextColor.YELLOW));
            for (IQuestObjective objective : currentStep.objectives) {
                String progressText = "";
                if (objective instanceof KillObjective killObj) {
                    int current = data.getQuestCounter(killObj.getProgressId());
                    int max = killObj.getCount();
                    progressText = " (" + current + "/" + max + ")";
                } else if (objective instanceof SlayObjective slayObj) {
                    int current = data.getQuestCounter(slayObj.getProgressId());
                    int max = slayObj.getCount();
                    progressText = " (" + current + "/" + max + ")";
                }

                Component objectiveComponent = Component.text(
                        "  - " + TKit.extractPlainText(objective.getDescription()) + progressText,
                        objective.isCompleted(player, data) ? NamedTextColor.GREEN : NamedTextColor.GRAY
                );
                player.sendMessage(objectiveComponent);
            }

        }, questIdArg);
    }
}

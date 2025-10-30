package org.example.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.QuestObjectiveCompleteEvent;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.utils.BookGuiManager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NpcInteractionCommand extends Command {

    public NpcInteractionCommand() {
        super("npc_interact");

        setCondition((sender, context) -> sender instanceof Player);

        var bookLiteral = ArgumentType.Literal("book");
        var talkLiteral = ArgumentType.Literal("talk");
        var startQuestLiteral = ArgumentType.Literal("start_quest");
        var advanceQuestLiteral = ArgumentType.Literal("advance_quest");
        var talkObjectiveLiteral = ArgumentType.Literal("talk_objective");

        var npcIdArg = ArgumentType.String("npcId");
        var questIdArg = ArgumentType.String("questId");

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            NPC npc = NpcRegistry.byId(npcId);
            if (npc != null) {
                BookGuiManager.openNpcBook(player, npc);
            }
        }, bookLiteral, npcIdArg);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            NPC npc = NpcRegistry.byId(npcId);
            if (npc != null && !npc.randomDialogues().isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(npc.randomDialogues().size());
                BookGuiManager.showDialogueBook(player, npc, List.of(npc.randomDialogues().get(randomIndex)));
            }
        }, talkLiteral, npcIdArg);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            String questId = context.get(questIdArg);
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null) return;

            Quest quest = QuestRegistry.byId(questId);
            if (quest != null) {
                QuestManager.tryStartQuest(player, data, quest, npcId);
            }
        }, startQuestLiteral, npcIdArg, questIdArg);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            String questId = context.get(questIdArg);
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null) return;

            Quest quest = QuestRegistry.byId(questId);
            if (quest != null) {
                data.quests.stream()
                        .filter(p -> p.questId.equals(questId))
                        .findFirst()
                        .ifPresent(progress -> QuestManager.tryAdvanceQuestByNpc(player, data, quest, progress, npcId));
            }
        }, advanceQuestLiteral, npcIdArg, questIdArg);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            String questId = context.get(questIdArg);
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null) return;

            Quest quest = QuestRegistry.byId(questId);
            if (quest == null) return;

            data.quests.stream()
                    .filter(p -> p.questId.equals(questId))
                    .findFirst()
                    .ifPresent(progress -> {
                        QuestStep currentStep = quest.steps.get(progress.stepIndex);
                        for (IQuestObjective objective : currentStep.objectives) {
                            if (objective instanceof TalkObjective talkObj && talkObj.getNpcId().equals(npcId) && !progress.isObjectiveCompleted(objective)) {
                                BookGuiManager.showDialogueBook(player, NpcRegistry.byId(npcId), talkObj.getDialogues());
                                progress.setObjectiveCompleted(objective, true);
                                QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(player, progress, currentStep, talkObj));
                                break;
                            }
                        }
                    });
        }, talkObjectiveLiteral, npcIdArg, questIdArg);
    }
}

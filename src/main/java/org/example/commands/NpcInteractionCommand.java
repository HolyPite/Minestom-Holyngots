package org.example.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal command to handle player interactions from the NPC dialogue menu.
 * This command is not meant to be typed by players.
 */
public class NpcInteractionCommand extends Command {

    public NpcInteractionCommand() {
        super("npc_interact");

        setCondition((sender,context) -> sender instanceof Player);

        // --- Argument Definitions ---
        var talkLiteral = ArgumentType.Literal("talk");
        var startQuestLiteral = ArgumentType.Literal("start_quest");
        var advanceQuestLiteral = ArgumentType.Literal("advance_quest");

        var npcIdArg = ArgumentType.String("npcId");
        var questIdArg = ArgumentType.String("questId");

        // --- Syntax 1: /npc_interact talk <npcId> ---
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String npcId = context.get(npcIdArg);
            NPC npc = NpcRegistry.byId(npcId);
            if (npc != null && !npc.randomDialogues().isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(npc.randomDialogues().size());
                player.sendMessage(npc.randomDialogues().get(randomIndex));
            }
        }, talkLiteral, npcIdArg);

        // --- Syntax 2: /npc_interact start_quest <npcId> <questId> ---
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

        // --- Syntax 3: /npc_interact advance_quest <npcId> <questId> ---
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
    }
}

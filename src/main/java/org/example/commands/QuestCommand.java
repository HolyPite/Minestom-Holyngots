package org.example.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.NodesManagement;
import org.example.data.PlayerDataService;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.registry.QuestRegistry;

import java.util.Optional;

/**
 * Basic command to start and progress quests for demonstration.
 */
public class QuestCommand extends Command {
    private final PlayerDataService dataService = NodesManagement.getDataService();

    public QuestCommand() {
        super("quest");

        var actionArg = ArgumentType.Word("action");
        var idArg = ArgumentType.String("id").setDefaultValue("");

        setDefaultExecutor((sender, ctx) -> sender.sendMessage("Usage: /quest <list|start|next> [id]"));

        addSyntax((sender, ctx) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Command only for players");
                return;
            }
            String action = ctx.get(actionArg);
            String id = ctx.get(idArg);
            PlayerData data = dataService.get(player);
            if (data == null) {
                sender.sendMessage("No data loaded");
                return;
            }

            switch (action) {
                case "list" -> QuestRegistry.all().forEach((qid, q) -> sender.sendMessage("- " + qid));
                case "start" -> startQuest(player, data, id);
                case "next" -> advanceQuest(player, data, id);
                default -> sender.sendMessage("Unknown action");
            }
        }, actionArg, idArg);
    }

    private void startQuest(Player player, PlayerData data, String id) {
        Quest quest = QuestRegistry.byId(id);
        if (quest == null) {
            player.sendMessage("Unknown quest: " + id);
            return;
        }
        boolean has = data.quests.stream().anyMatch(p -> p.questId.equals(id));
        if (has) {
            player.sendMessage("Already started");
            return;
        }
        data.quests.add(new QuestProgress(id));
        player.sendMessage("Quest started: " + id);
    }

    private void advanceQuest(Player player, PlayerData data, String id) {
        Optional<QuestProgress> opt = data.quests.stream().filter(p -> p.questId.equals(id)).findFirst();
        if (opt.isEmpty()) {
            player.sendMessage("Quest not started: " + id);
            return;
        }
        QuestProgress prog = opt.get();
        prog.stepIndex++;
        player.sendMessage("Step advanced to " + prog.stepIndex);
    }
}

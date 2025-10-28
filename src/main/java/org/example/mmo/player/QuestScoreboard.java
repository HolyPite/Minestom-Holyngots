package org.example.mmo.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.objectives.FetchObjective;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.objectives.SlayObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.utils.TKit;

import java.util.ArrayList;

public class QuestScoreboard {

    private final Sidebar sidebar;
    private final Player player;

    public QuestScoreboard(Player player) {
        this.player = player;
        this.sidebar = new Sidebar(Component.text("Quêtes", NamedTextColor.GOLD));
    }

    public void update(PlayerData data) {
        for (Sidebar.ScoreboardLine line : new ArrayList<>(sidebar.getLines())) {
            sidebar.removeLine(line.getId());
        }

        int line = 0;
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null) continue;

            sidebar.createLine(new Sidebar.ScoreboardLine("quest_" + line++, Component.text(TKit.extractPlainText(quest.name), NamedTextColor.YELLOW), -line));

            if (progress.stepIndex < quest.steps.size()) {
                QuestStep currentStep = quest.steps.get(progress.stepIndex);

                if (currentStep.objectives.isEmpty()) {
                    NPC endNpc = NpcRegistry.byId(currentStep.endNpc);
                    String npcName = (endNpc != null) ? TKit.extractPlainText(endNpc.name()) : "???";
                    Component lineText = Component.text("  - Parler à " + npcName, NamedTextColor.GRAY);
                    sidebar.createLine(new Sidebar.ScoreboardLine("obj_" + line++, lineText, -line));
                } else {
                    boolean allObjectivesCompleted = true;
                    for (IQuestObjective objective : currentStep.objectives) {
                        String progressText = "";
                        if (objective instanceof KillObjective killObj) {
                            progressText = String.format(" %d/%d", data.getQuestCounter(killObj.getProgressId()), killObj.getCount());
                        } else if (objective instanceof SlayObjective slayObj) {
                            progressText = String.format(" %d/%d", data.getQuestCounter(slayObj.getProgressId()), slayObj.getCount());
                        } else if (objective instanceof LocationObjective locObj) {
                            Pos target = locObj.getCenter();
                            progressText = String.format(" (%d, %d, %d)", target.blockX(), target.blockY(), target.blockZ());
                        } else if (objective instanceof FetchObjective fetchObj) {
                            int currentAmount = TKit.countItems(player, fetchObj.getItemToFetch().toItemStack());
                            progressText = String.format(" %d/%d", currentAmount, fetchObj.getRequiredAmount());
                        }

                        boolean isCompleted = progress.isObjectiveCompleted(objective);
                        if (!isCompleted) allObjectivesCompleted = false;

                        Component lineText = Component.text("  - " + TKit.extractPlainText(objective.getDescription()) + progressText, isCompleted ? NamedTextColor.GREEN : NamedTextColor.GRAY);
                        sidebar.createLine(new Sidebar.ScoreboardLine("obj_" + line++, lineText, -line));
                    }

                    if (allObjectivesCompleted && currentStep.endNpc != null && !currentStep.endNpc.isEmpty()) {
                        NPC endNpc = NpcRegistry.byId(currentStep.endNpc);
                        if (endNpc != null) {
                            Component returnText = Component.text("  -> Retournez voir ", NamedTextColor.YELLOW).append(endNpc.name());
                            sidebar.createLine(new Sidebar.ScoreboardLine("return_" + line++, returnText, -line));
                        }
                    }
                }
            }
        }

        if (!sidebar.getLines().isEmpty()) {
            sidebar.addViewer(player);
        } else {
            sidebar.removeViewer(player);
        }
    }
}

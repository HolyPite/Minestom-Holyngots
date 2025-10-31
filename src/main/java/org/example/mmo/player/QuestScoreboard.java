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

        final int maxLines = 15;
        boolean truncated = false;
        java.util.List<Component> lines = new ArrayList<>(maxLines);

        outer:
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null) {
                continue;
            }

            if (!addLine(lines, Component.text(TKit.extractPlainText(quest.name), NamedTextColor.YELLOW), maxLines)) {
                truncated = true;
                break;
            }

            if (progress.stepIndex >= quest.steps.size()) {
                continue;
            }

            QuestStep currentStep = quest.steps.get(progress.stepIndex);

            if (currentStep.objectives.isEmpty()) {
                if (!addLine(lines, describeNpcObjective(currentStep), maxLines)) {
                    truncated = true;
                    break;
                }
                continue;
            }

            boolean allObjectivesCompleted = true;
            for (IQuestObjective objective : currentStep.objectives) {
                Component lineText = describeObjective(progress, data, objective);
                if (!addLine(lines, lineText, maxLines)) {
                    truncated = true;
                    break outer;
                }

                if (!progress.isObjectiveCompleted(objective)) {
                    allObjectivesCompleted = false;
                }
            }

            if (allObjectivesCompleted && currentStep.endNpc != null && !currentStep.endNpc.isEmpty()) {
                NPC endNpc = NpcRegistry.byId(currentStep.endNpc);
                if (endNpc != null) {
                    Component returnText = Component.text("  -> Retournez voir ", NamedTextColor.YELLOW).append(endNpc.name());
                    if (!addLine(lines, returnText, maxLines)) {
                        truncated = true;
                        break;
                    }
                }
            }
        }

        if (truncated) {
            if (!lines.isEmpty()) {
                lines.set(lines.size() - 1, Component.text("... (journal abrégé)", NamedTextColor.DARK_GRAY));
            } else {
                lines.add(Component.text("... (journal abrégé)", NamedTextColor.DARK_GRAY));
            }
        }

        if (!sidebar.getLines().isEmpty()) {
            sidebar.addViewer(player);
        } else {
            sidebar.removeViewer(player);
        }
    }

    private Component describeNpcObjective(QuestStep currentStep) {
        NPC endNpc = NpcRegistry.byId(currentStep.endNpc);
        String npcName = (endNpc != null) ? TKit.extractPlainText(endNpc.name()) : "???";
        return Component.text("  - Parler à " + npcName, NamedTextColor.GRAY);
    }

    private Component describeObjective(QuestProgress progress, PlayerData data, IQuestObjective objective) {
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
        return Component.text("  - " + TKit.extractPlainText(objective.getDescription()) + progressText,
                isCompleted ? NamedTextColor.GREEN : NamedTextColor.GRAY);
    }

    private boolean addLine(java.util.List<Component> lines, Component text, int maxLines) {
        if (lines.size() >= maxLines) {
            return false;
        }
        int index = lines.size();
        lines.add(text);
        // Rebuild sidebar line immediately to keep ordering without extra pass
        String id = "line_" + index;
        int score = maxLines - index;
        sidebar.createLine(new Sidebar.ScoreboardLine(id, text, score));
        return true;
    }
}

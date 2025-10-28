package org.example.mmo.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.timer.TaskSchedule;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.event.QuestObjectiveCompleteEvent;
import org.example.mmo.quest.event.QuestObjectiveProgressEvent;
import org.example.mmo.quest.objectives.FetchObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

public class InventoryListener {

    public static void init(EventNode<InventoryEvent> eventNode) {
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null) return;

            // Use a delayed task to check after the inventory has been updated
            player.scheduler().buildTask(() -> {
                boolean needsScoreboardUpdate = false;
                for (QuestProgress progress : data.quests) {
                    Quest quest = QuestRegistry.byId(progress.questId);
                    if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

                    QuestStep currentStep = quest.steps.get(progress.stepIndex);
                    for (var objective : currentStep.objectives) {
                        if (objective instanceof FetchObjective fetchObj) {
                            needsScoreboardUpdate = true; // A fetch objective is active, so the scoreboard might need an update

                            boolean wasCompleted = progress.isObjectiveCompleted(fetchObj);
                            boolean isNowCompleted = fetchObj.isCompleted(player, data);

                            if (!wasCompleted && isNowCompleted) {
                                // Objective has just been completed
                                progress.setObjectiveCompleted(fetchObj, true);
                                QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(player, progress, currentStep, fetchObj));
                            } else if (wasCompleted && !isNowCompleted) {
                                // Objective is no longer completed (player dropped items)
                                progress.setObjectiveCompleted(fetchObj, false);
                            }
                        }
                    }
                }
                if (needsScoreboardUpdate) {
                    // Always fire a progress event to update the scoreboard numbers
                    QuestManager.getEventNode().call(new QuestObjectiveProgressEvent(player));
                }
            }).delay(TaskSchedule.tick(1)).schedule();
        });
    }
}

package org.example.mmo.quest.service;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

import java.util.ArrayList;

public final class QuestTimerService {

    private QuestTimerService() {
    }

    public static void schedule() {
        MinecraftServer.getSchedulerManager()
                .buildTask(QuestTimerService::checkQuestTimers)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    private static void checkQuestTimers() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            PlayerData data = GameContext.get().playerDataService().get(player);
            if (data == null || data.quests.isEmpty()) {
                continue;
            }

            for (QuestProgress progress : new ArrayList<>(data.quests)) {
                Quest quest = QuestRegistry.byId(progress.questId);
                if (quest == null || progress.stepIndex >= quest.steps.size()) {
                    continue;
                }

                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                if (currentStep.duration.isZero()) {
                    continue;
                }

                boolean allObjectivesMet = currentStep.objectives.stream().allMatch(progress::isObjectiveCompleted);
                if (allObjectivesMet) {
                    continue;
                }

                long timeElapsed = System.currentTimeMillis() - progress.stepStartTime;
                if (timeElapsed > currentStep.duration.toMillis()) {
                    QuestManager.handleQuestFailure(player, data, quest, progress);
                }
            }
        }
    }
}

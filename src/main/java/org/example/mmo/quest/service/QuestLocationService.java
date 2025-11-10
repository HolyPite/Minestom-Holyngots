package org.example.mmo.quest.service;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.Material;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.QuestObjectiveCompleteEvent;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.utils.ToastManager;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestLocationService {

    private static final Set<UUID> TRACKED_PLAYERS = ConcurrentHashMap.newKeySet();

    private QuestLocationService() {
    }

    public static void register(EventNode<Event> eventNode) {
        eventNode.addListener(PlayerMoveEvent.class, QuestLocationService::handlePlayerMove);
        eventNode.addListener(QuestObjectiveCompleteEvent.class, QuestLocationService::handleObjectiveCompletion);
    }

    public static void track(Player player) {
        TRACKED_PLAYERS.add(player.getUuid());
    }

    public static void untrack(Player player) {
        TRACKED_PLAYERS.remove(player.getUuid());
    }

    public static void refreshTracking(Player player, PlayerData data) {
        UUID uuid = player.getUuid();
        if (data == null) {
            TRACKED_PLAYERS.remove(uuid);
            return;
        }

        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) {
                continue;
            }

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof LocationObjective && !progress.isObjectiveCompleted(objective)) {
                    TRACKED_PLAYERS.add(uuid);
                    return;
                }
            }
        }

        TRACKED_PLAYERS.remove(uuid);
    }

    private static void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!TRACKED_PLAYERS.contains(player.getUuid())) {
            return;
        }

        PlayerData data = GameContext.get().playerDataService().get(player);
        if (data == null) {
            return;
        }

        for (QuestProgress progress : new ArrayList<>(data.quests)) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) {
                continue;
            }

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof LocationObjective locObj) {
                    if (locObj.isCompleted(player, data) && !progress.isObjectiveCompleted(locObj)) {
                        progress.setObjectiveCompleted(locObj, true);
                        QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(player, progress, currentStep, locObj));
                    }
                }
            }
        }
    }

    private static void handleObjectiveCompletion(QuestObjectiveCompleteEvent event) {
        Player player = event.getPlayer();
        PlayerData data = GameContext.get().playerDataService().get(player);
        QuestStep completedStep = event.getCompletedStep();

        player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.NEUTRAL, 1f, 1.5f), player.getPosition());
        event.getCompletedObjective().onComplete(player, data);

        ToastManager.showToast(player, event.getCompletedObjective().getDescription(), Material.WRITABLE_BOOK, FrameType.GOAL);

        boolean allComplete = completedStep.objectives.stream().allMatch(obj -> event.getQuestProgress().isObjectiveCompleted(obj));

        if (allComplete) {
            if (event.getQuestProgress().stepCompletionTime == 0L) {
                event.getQuestProgress().stepCompletionTime = System.currentTimeMillis();
            }
            if (completedStep.endNpc == null || completedStep.endNpc.isEmpty()) {
                Quest quest = QuestRegistry.byId(event.getQuestProgress().questId);
                QuestManager.advanceToStep(player, data, quest, event.getQuestProgress(), event.getQuestProgress().stepIndex + 1);
            }
        }
    }
}

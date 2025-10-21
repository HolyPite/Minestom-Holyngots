package org.example.mmo.quest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.combat.DamageHistory;
import org.example.mmo.combat.DamageRecord;
import org.example.mmo.combat.DamageTracker;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.objectives.SlayObjective;
import org.example.utils.TKit;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestManager {

    public static final Tag<String> NPC_ID_TAG = Tag.String("quest_npc_id");
    private static final Set<Player> playersWithLocationObjectives = ConcurrentHashMap.newKeySet();
    private static EventNode<Event> EVENT_NODE;

    public static void init(EventNode<Event> eventNode) {
        EVENT_NODE = eventNode;
        EVENT_NODE.addListener(PlayerEntityInteractEvent.class, QuestManager::handleNpcInteraction);
        EVENT_NODE.addListener(EntityDeathEvent.class, QuestManager::handleEntityDeath);
        EVENT_NODE.addListener(PlayerKillEntityEvent.class, QuestManager::handlePlayerKill);
        EVENT_NODE.addListener(PlayerMoveEvent.class, QuestManager::handlePlayerMove);
        EVENT_NODE.addListener(QuestObjectiveCompleteEvent.class, QuestManager::handleObjectiveCompletion);

        MinecraftServer.getSchedulerManager().buildTask(QuestManager::checkQuestTimers).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void handleNpcInteraction(PlayerEntityInteractEvent event) {
        Player player = event.getPlayer();
        String npcId = event.getTarget().getTag(NPC_ID_TAG);
        if (npcId == null || npcId.isEmpty()) return;

        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) return;

        QuestRegistry.all().forEach((questId, quest) -> {
            QuestProgress progress = data.quests.stream().filter(p -> p.questId.equals(questId)).findFirst().orElse(null);
            if (progress == null) {
                tryStartQuest(player, data, quest, npcId);
            } else {
                tryAdvanceQuestByNpc(player, data, quest, progress, npcId);
            }
        });
    }

    private static void tryStartQuest(Player player, PlayerData data, Quest quest, String npcId) {
        if (quest.steps.isEmpty() || !npcId.equals(quest.steps.getFirst().startNpc)) return;
        if (data.hasFailedQuest(quest.id)) return;

        if (quest.repeatable) {
            Long cooldownTime = data.questCooldowns.get(quest.id);
            if (cooldownTime != null) {
                long timeSinceCompletion = System.currentTimeMillis() - cooldownTime;
                if (timeSinceCompletion < quest.cooldown.toMillis()) {
                    long remainingSeconds = (quest.cooldown.toMillis() - timeSinceCompletion) / 1000;
                    player.sendMessage(Component.text("Vous devez encore attendre " + TKit.formatTime((int) remainingSeconds) + " minutes.", NamedTextColor.YELLOW));
                    return;
                }
            }
        } else if (data.hasCompletedQuest(quest.id)) {
            return; // Not repeatable and already completed
        }

        if (data.level < quest.requiredLevel) {
            player.sendMessage(Component.text("Vous n'avez pas le niveau requis.", NamedTextColor.RED));
            return;
        }

        QuestProgress newProgress = new QuestProgress(quest.id);
        if (advanceToStep(player, data, quest, newProgress, 0)) {
            data.quests.add(newProgress);
        }
    }

    private static void tryAdvanceQuestByNpc(Player player, PlayerData data, Quest quest, QuestProgress progress, String npcId) {
        if (progress.stepIndex >= quest.steps.size()) return;
        QuestStep currentStep = quest.steps.get(progress.stepIndex);
        if (!npcId.equals(currentStep.endNpc)) return;

        if (currentStep.objectives.stream().allMatch(obj -> obj.isCompleted(player, data))) {
            advanceToStep(player, data, quest, progress, progress.stepIndex + 1);
        } else {
            currentStep.waitingDialogues.forEach(player::sendMessage);
        }
    }

    private static boolean advanceToStep(Player player, PlayerData data, Quest quest, QuestProgress progress, int newStepIndex) {
        if (newStepIndex > 0 && progress.stepIndex < quest.steps.size()) {
            QuestStep oldStep = quest.steps.get(progress.stepIndex);
            oldStep.successDialogues.forEach(player::sendMessage);
            oldStep.objectives.forEach(obj -> obj.onComplete(player, data));
            oldStep.rewards.forEach(reward -> reward.apply(player));
            playersWithLocationObjectives.remove(player);
        }

        if (newStepIndex >= quest.steps.size()) {
            player.sendMessage(Component.text("Quête terminée : ").append(quest.name));
            data.quests.removeIf(p -> p.questId.equals(quest.id));
            if (quest.repeatable) {
                data.questCooldowns.put(quest.id, System.currentTimeMillis());
            } else if (!data.hasCompletedQuest(quest.id)) {
                data.completedQuests.add(quest.id);
            }
            return true;
        }

        QuestStep newStep = quest.steps.get(newStepIndex);
        if (!checkPrerequisites(data, newStep)) {
            player.sendMessage(Component.text("Vous ne remplissez pas les conditions.", NamedTextColor.RED));
            return false;
        }
        if (!newStep.delay.isZero() && newStepIndex > 0) {
            long timeSinceLastStep = System.currentTimeMillis() - progress.stepStartTime;
            if (timeSinceLastStep < newStep.delay.toMillis()) {
                newStep.delayDialogues.forEach(player::sendMessage);
                return false;
            }
        }

        progress.stepIndex = newStepIndex;
        progress.stepStartTime = System.currentTimeMillis();
        player.sendMessage(Component.text("Nouvel objectif : ").append(newStep.name != null ? newStep.name : Component.empty()));
        player.sendMessage(newStep.description);
        newStep.objectives.forEach(obj -> {
            obj.onStart(player, data);
            if (obj instanceof LocationObjective) playersWithLocationObjectives.add(player);
        });
        return true;
    }

    private static void handleQuestFailure(Player player, PlayerData data, Quest quest, QuestProgress progress) {
        QuestStep currentStep = quest.steps.get(progress.stepIndex);
        currentStep.failureDialogues.forEach(player::sendMessage);
        progress.attempts++;

        if (currentStep.attemptLimit > 0 && progress.attempts >= currentStep.attemptLimit) {
            player.sendMessage(Component.text("Vous avez échoué la quête définitivement.", NamedTextColor.DARK_RED));
            data.quests.remove(progress);
            data.failedQuests.add(quest.id);
            playersWithLocationObjectives.remove(player);

            if (currentStep.failureRedirection && currentStep.failureRedirectionQuest != null) {
                Quest redirectionQuest = QuestRegistry.byId(currentStep.failureRedirectionQuest);
                if (redirectionQuest != null) tryStartQuest(player, data, redirectionQuest, "");
            }
        } else {
            player.sendMessage(Component.text("Vous pouvez réessayer.", NamedTextColor.YELLOW));
            currentStep.objectives.forEach(obj -> obj.onReset(player, data));
            progress.stepStartTime = System.currentTimeMillis();
        }
    }

    private static void checkQuestTimers() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null || data.quests.isEmpty()) continue;

            for (QuestProgress progress : new ArrayList<>(data.quests)) { // Use a safe copy to prevent ConcurrentModificationException
                Quest quest = QuestRegistry.byId(progress.questId);
                if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                if (!currentStep.duration.isZero()) {
                    long timeElapsed = System.currentTimeMillis() - progress.stepStartTime;
                    if (timeElapsed > currentStep.duration.toMillis()) {
                        handleQuestFailure(player, data, quest, progress);
                    }
                }
            }
        }
    }

    private static boolean checkPrerequisites(PlayerData data, QuestStep step) {
        if (step.prerequisites == null || step.prerequisites.isEmpty()) return true;
        for (String prereq : step.prerequisites) {
            String[] parts = prereq.split(":");
            String requiredQuestId = parts[0];
            if (parts.length > 1) {
                try {
                    if (!data.hasReachedQuestStep(requiredQuestId, Integer.parseInt(parts[1]))) return false;
                } catch (NumberFormatException e) { return false; }
            } else {
                if (!data.hasCompletedQuest(requiredQuestId)) return false;
            }
        }
        return true;
    }

    private static void handleEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity deadEntity) {
            DamageHistory history = DamageTracker.getHistory(deadEntity);
            if (history != null) {
                Player killer = history.findLastPlayerAttacker();
                if (killer != null) {
                    EVENT_NODE.call(new PlayerKillEntityEvent(killer, deadEntity));
                }
            }
        }
    }

    private static void handlePlayerKill(PlayerKillEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) return;

        for (QuestProgress progress : new ArrayList<>(data.quests)) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                // Handle simple KillObjective
                if (objective instanceof KillObjective killObj && killObj.getEntityType() == event.getKilled().getEntityType()) {
                    if (tryIncrementObjective(player, data, progress, currentStep, killObj, killObj.getProgressId(), killObj.getCount())) {
                        EVENT_NODE.call(new QuestObjectiveCompleteEvent(player, progress, currentStep, killObj));
                    }
                }
                // Handle conditional SlayObjective
                else if (objective instanceof SlayObjective slayObj && slayObj.getEntityType() == event.getKilled().getEntityType()) {
                    DamageHistory history = DamageTracker.getHistory(event.getKilled());
                    if (history != null) {
                        DamageRecord lastDamage = history.getLastDamage();
                        if (lastDamage != null && slayObj.getKillCondition().test(lastDamage.damage())) {
                            if (tryIncrementObjective(player, data, progress, currentStep, slayObj, slayObj.getProgressId(), slayObj.getCount())) {
                                EVENT_NODE.call(new QuestObjectiveCompleteEvent(player, progress, currentStep, slayObj));
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean tryIncrementObjective(Player player, PlayerData data, QuestProgress progress, QuestStep step, IQuestObjective objective, String progressId, int requiredCount) {
        if (objective.isCompleted(player, data)) return false;

        data.incrementQuestCounter(progressId, 1);
        int currentCount = data.getQuestCounter(progressId);

        Component feedback = Component.text(step.name != null ? TKit.extractPlainText(step.name) + ": " : "", NamedTextColor.GRAY)
                .append(Component.text(currentCount, NamedTextColor.WHITE))
                .append(Component.text(" / ", NamedTextColor.GRAY))
                .append(Component.text(requiredCount, NamedTextColor.WHITE));
        player.sendMessage(feedback);

        return currentCount >= requiredCount;
    }

    private static void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playersWithLocationObjectives.contains(player)) return;

        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) return;

        for (QuestProgress progress : new ArrayList<>(data.quests)) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof LocationObjective locObj && !locObj.isCompleted(player, data)) {
                     if (player.getPosition().distanceSquared(locObj.getCenter()) <= locObj.getRadius() * locObj.getRadius()) {
                         EVENT_NODE.call(new QuestObjectiveCompleteEvent(player, progress, currentStep, locObj));
                     }
                }
            }
        }
    }

    private static void handleObjectiveCompletion(QuestObjectiveCompleteEvent event) {
        Player player = event.getPlayer();
        PlayerData data = NodesManagement.getDataService().get(player);
        QuestStep completedStep = event.getCompletedStep();

        boolean allComplete = completedStep.objectives.stream().allMatch(obj -> obj.isCompleted(player, data));

        if (allComplete) {
            if (completedStep.endNpc == null || completedStep.endNpc.isEmpty()) {
                Quest quest = QuestRegistry.byId(event.getQuestProgress().questId);
                advanceToStep(player, data, quest, event.getQuestProgress(), event.getQuestProgress().stepIndex + 1);
            }
        }
    }
}

package org.example.mmo.quest;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.combat.history.DamageHistory;
import org.example.mmo.combat.history.DamageRecord;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.*;
import org.example.mmo.quest.objectives.*;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.utils.BookGuiManager;
import org.example.utils.TKit;
import org.example.utils.ToastManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestManager {

    public static final Tag<String> NPC_ID_TAG = Tag.String("quest_npc_id");
    public static final Set<UUID> playersWithLocationObjectives = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> lastInteractionTime = new ConcurrentHashMap<>();
    private static final long INTERACTION_COOLDOWN = 1000; // 1 second cooldown
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

    public static EventNode<Event> getEventNode() {
        return EVENT_NODE;
    }

    private static void handleNpcInteraction(PlayerEntityInteractEvent event) {
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        long lastTime = lastInteractionTime.getOrDefault(player.getUuid(), 0L);

        if (now - lastTime < INTERACTION_COOLDOWN) {
            return;
        }
        lastInteractionTime.put(player.getUuid(), now);

        String npcId = event.getTarget().getTag(NPC_ID_TAG);
        if (npcId == null || npcId.isEmpty()) return;

        NPC npc = NpcRegistry.byId(npcId);
        if (npc == null) return;

        player.playSound(npc.soundEffect(), player.getPosition());
        BookGuiManager.openNpcBook(player, npc);
    }

    public static void tryStartQuest(Player player, PlayerData data, Quest quest, String npcId) {
        if (data.quests.stream().anyMatch(p -> p.questId.equals(quest.id))) {
            return;
        }

        if (quest.steps.isEmpty() || !npcId.equals(quest.steps.getFirst().startNpc)) return;
        if (data.hasFailedQuest(quest.id)) return;

        if (quest.repeatable) {
            Long cooldownTime = data.questCooldowns.get(quest.id);
            if (cooldownTime != null) {
                long timeSinceCompletion = System.currentTimeMillis() - cooldownTime;
                if (timeSinceCompletion < quest.cooldown.toMillis()) {
                    ToastManager.showToast(player, Component.text("Vous devez encore attendre " + TKit.formatTime((int) ((quest.cooldown.toMillis() - timeSinceCompletion) / 1000)) + " minutes."), Material.CLOCK, FrameType.TASK);
                    return;
                }
            }
        } else if (data.hasCompletedQuest(quest.id)) {
            return;
        }

        if (data.level < quest.requiredLevel) {
            ToastManager.showToast(player, Component.text("Niveau requis : " + quest.requiredLevel), Material.BARRIER, FrameType.TASK);
            return;
        }

        QuestProgress newProgress = new QuestProgress(quest.id);
        data.quests.add(newProgress);
        if (advanceToStep(player, data, quest, newProgress, 0)) {
            EVENT_NODE.call(new QuestStartEvent(player, quest));
        }
    }

    public static boolean tryAutoStartQuest(Player player, PlayerData data, Quest quest) {
        if (quest.steps.isEmpty() || (quest.steps.getFirst().startNpc != null && !quest.steps.getFirst().startNpc.isEmpty())) {
            return false;
        }

        if (data.quests.stream().anyMatch(p -> p.questId.equals(quest.id))) return false;
        if (data.hasFailedQuest(quest.id)) return false;
        if (data.hasCompletedQuest(quest.id) && !quest.repeatable) return false;
        if (data.level < quest.requiredLevel) return false;
        if (!checkPrerequisites(data, quest.steps.getFirst())) return false;

        if (quest.repeatable) {
            Long cooldownTime = data.questCooldowns.get(quest.id);
            if (cooldownTime != null) {
                long timeSinceCompletion = System.currentTimeMillis() - cooldownTime;
                if (timeSinceCompletion < quest.cooldown.toMillis()) {
                    return false;
                }
            }
        }

        QuestProgress newProgress = new QuestProgress(quest.id);
        data.quests.add(newProgress);
        if (advanceToStep(player, data, quest, newProgress, 0)) {
            EVENT_NODE.call(new QuestStartEvent(player, quest));
            return true;
        }
        return false;
    }

    public static void tryAdvanceQuestByNpc(Player player, PlayerData data, Quest quest, QuestProgress progress, String npcId) {
        if (progress.stepIndex >= quest.steps.size()) return;
        QuestStep currentStep = quest.steps.get(progress.stepIndex);
        if (!npcId.equals(currentStep.endNpc)) return;

        if (currentStep.objectives.stream().allMatch(obj -> progress.isObjectiveCompleted(obj))) {
            advanceToStep(player, data, quest, progress, progress.stepIndex + 1);
        } else {
            BookGuiManager.showDialogueBook(player, NpcRegistry.byId(npcId), quest, currentStep, currentStep.waitingDialogues);
        }
    }

    private static boolean advanceToStep(Player player, PlayerData data, Quest quest, QuestProgress progress, int newStepIndex) {
        if (newStepIndex > 0 && progress.stepIndex < quest.steps.size()) {
            QuestStep oldStep = quest.steps.get(progress.stepIndex);
            if (!oldStep.successDialogues.isEmpty()) {
                NPC dialogueNpc = null;
                if (oldStep.endNpc != null) {
                    dialogueNpc = NpcRegistry.byId(oldStep.endNpc);
                }
                if (dialogueNpc == null && oldStep.startNpc != null) {
                    dialogueNpc = NpcRegistry.byId(oldStep.startNpc);
                }
                BookGuiManager.showDialogueBook(player, dialogueNpc, quest, oldStep, oldStep.successDialogues);
            }

            for (IQuestObjective objective : oldStep.objectives) {
                if (objective instanceof FetchObjective fetchObj) {
                    TKit.removeItems(player, fetchObj.getItemToFetch().toItemStack(), fetchObj.getRequiredAmount());
                } else if (objective instanceof KillObjective killObj) {
                    data.questCounters.remove(killObj.getProgressId());
                } else if (objective instanceof SlayObjective slayObj) {
                    data.questCounters.remove(slayObj.getProgressId());
                }
                objective.onComplete(player, data);
            }

            oldStep.rewards.forEach(reward -> reward.apply(player));
            playersWithLocationObjectives.remove(player.getUuid());
            progress.resetObjectiveCompletionStatus();
        }

        if (newStepIndex >= quest.steps.size()) {
            ToastManager.showToast(player, quest.name, Material.CHEST, FrameType.CHALLENGE);
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER, 0.8f, 1f), player.getPosition());
            data.quests.removeIf(p -> p.questId.equals(quest.id));
            if (quest.repeatable) {
                data.questCooldowns.put(quest.id, System.currentTimeMillis());
            } else if (!data.hasCompletedQuest(quest.id)) {
                data.completedQuests.add(quest.id);
            }
            EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
            QuestRegistry.autoStartQuests().forEach(q -> tryAutoStartQuest(player, data, q));
            return true;
        }

        QuestStep newStep = quest.steps.get(newStepIndex);
        if (!checkPrerequisites(data, newStep)) {
            ToastManager.showToast(player, Component.text("Vous ne remplissez pas les conditions."), Material.BARRIER, FrameType.TASK);
            return false;
        }
        if (!newStep.delay.isZero() && newStepIndex > 0) {
            long timeSinceLastStep = System.currentTimeMillis() - progress.stepStartTime;
            if (timeSinceLastStep < newStep.delay.toMillis()) {
                BookGuiManager.showDialogueBook(player, NpcRegistry.byId(newStep.startNpc), quest, newStep, newStep.delayDialogues);
                return false;
            }
        }

        progress.stepIndex = newStepIndex;
        progress.stepStartTime = System.currentTimeMillis();
        progress.resetObjectiveCompletionStatus();

        if (newStepIndex == 0) {
            ToastManager.showToast(player, quest.name, Material.BOOK, FrameType.TASK);
        }
        player.sendMessage(newStep.description.color(NamedTextColor.WHITE));

        newStep.objectives.forEach(obj -> {
            obj.onStart(player, data);
            if (obj instanceof LocationObjective) playersWithLocationObjectives.add(player.getUuid());
        });

        EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
        QuestRegistry.autoStartQuests().forEach(q -> tryAutoStartQuest(player, data, q));
        return true;
    }

    private static void handleQuestFailure(Player player, PlayerData data, Quest quest, QuestProgress progress) {
        QuestStep currentStep = quest.steps.get(progress.stepIndex);
        BookGuiManager.showDialogueBook(player, NpcRegistry.byId(currentStep.startNpc), quest, currentStep, currentStep.failureDialogues);
        progress.attempts++;

        if (currentStep.attemptLimit > 0 && progress.attempts >= currentStep.attemptLimit) {
            data.quests.remove(progress);
            data.failedQuests.add(quest.id);
            playersWithLocationObjectives.remove(player.getUuid());
            currentStep.objectives.forEach(obj -> obj.onComplete(player, data));
            EVENT_NODE.call(new QuestFailEvent(player, quest));
        } else {
            currentStep.objectives.forEach(obj -> obj.onReset(player, data));
            progress.stepStartTime = System.currentTimeMillis();
            progress.resetObjectiveCompletionStatus();
        }
    }

    private static void checkQuestTimers() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            PlayerData data = NodesManagement.getDataService().get(player);
            if (data == null || data.quests.isEmpty()) continue;

            for (QuestProgress progress : new ArrayList<>(data.quests)) {
                Quest quest = QuestRegistry.byId(progress.questId);
                if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                if (!currentStep.duration.isZero()) {
                    boolean allObjectivesMet = currentStep.objectives.stream().allMatch(obj -> progress.isObjectiveCompleted(obj));
                    if (allObjectivesMet) continue;

                    long timeElapsed = System.currentTimeMillis() - progress.stepStartTime;
                    if (timeElapsed > currentStep.duration.toMillis()) {
                        handleQuestFailure(player, data, quest, progress);
                    }
                }
            }
        }
    }

    public static boolean checkPrerequisites(PlayerData data, QuestStep step) {
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
                if (objective instanceof KillObjective killObj && killObj.getEntityType() == event.getKilled().getEntityType()) {
                    if (tryIncrementObjective(player, data, quest, progress, currentStep, killObj, killObj.getProgressId(), killObj.getCount())) {
                        EVENT_NODE.call(new QuestObjectiveCompleteEvent(player, progress, currentStep, killObj));
                    }
                } else if (objective instanceof SlayObjective slayObj && slayObj.getEntityType() == event.getKilled().getEntityType()) {
                    DamageHistory history = DamageTracker.getHistory(event.getKilled());
                    if (history != null) {
                        DamageRecord lastDamage = history.getLastDamage();
                        if (lastDamage != null && slayObj.getKillCondition().test(lastDamage.damage())) {
                            if (tryIncrementObjective(player, data, quest, progress, currentStep, slayObj, slayObj.getProgressId(), slayObj.getCount())) {
                                EVENT_NODE.call(new QuestObjectiveCompleteEvent(player, progress, currentStep, slayObj));
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean tryIncrementObjective(Player player, PlayerData data, Quest quest, QuestProgress progress, QuestStep step, IQuestObjective objective, String progressId, int requiredCount) {
        if (progress.isObjectiveCompleted(objective)) return false;

        data.incrementQuestCounter(progressId, 1);

        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.NEUTRAL, 0.5f, 1.2f), player.getPosition());

        boolean isNowComplete = data.getQuestCounter(progressId) >= requiredCount;
        if (isNowComplete) {
            progress.setObjectiveCompleted(objective, true);
        }

        EVENT_NODE.call(new QuestObjectiveProgressEvent(player));

        return isNowComplete;
    }

    private static void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playersWithLocationObjectives.contains(player.getUuid())) return;

        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) return;

        for (QuestProgress progress : new ArrayList<>(data.quests)) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof LocationObjective locObj) {
                    if (locObj.isCompleted(player, data) && !progress.isObjectiveCompleted(locObj)) {
                        progress.setObjectiveCompleted(locObj, true);
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

        player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.NEUTRAL, 1f, 1.5f), player.getPosition());
        event.getCompletedObjective().onComplete(player, data);

        ToastManager.showToast(player, event.getCompletedObjective().getDescription(), Material.WRITABLE_BOOK, FrameType.GOAL);

        boolean allComplete = completedStep.objectives.stream().allMatch(obj -> event.getQuestProgress().isObjectiveCompleted(obj));

        if (allComplete) {
            if (completedStep.endNpc == null || completedStep.endNpc.isEmpty()) {
                Quest quest = QuestRegistry.byId(event.getQuestProgress().questId);
                advanceToStep(player, data, quest, event.getQuestProgress(), event.getQuestProgress().stepIndex + 1);
            } else {
                // This message is fine as it's a direct instruction after completing all objectives.
                player.sendMessage(Component.text("Tous les objectifs sont remplis. Retournez voir ", NamedTextColor.YELLOW).append(NpcRegistry.byId(completedStep.endNpc).name()));
            }
        }
    }
}

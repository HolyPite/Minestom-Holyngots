package org.example.mmo.quest;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.*;
import org.example.mmo.quest.objectives.*;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.service.QuestCombatService;
import org.example.mmo.quest.service.QuestLocationService;
import org.example.mmo.quest.service.QuestNpcInteractionService;
import org.example.mmo.quest.service.QuestTimerService;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;
import org.example.mmo.npc.dialog.NpcDialogService;
import org.example.utils.TKit;
import org.example.utils.ToastManager;

import java.util.*;

public final class QuestManager {

    public static final Tag<String> NPC_ID_TAG = Tag.String("quest_npc_id");



    private static EventNode<Event> EVENT_NODE;

    public static void init(EventNode<Event> eventNode) {
        EVENT_NODE = eventNode;
        QuestNpcInteractionService.register(eventNode);
        QuestCombatService.register(eventNode);
        QuestLocationService.register(eventNode);
        QuestTimerService.schedule();
    }

    public static EventNode<Event> getEventNode() {
        return EVENT_NODE;
    }

    public static void trackLocationObjective(Player player) {
        QuestLocationService.track(player);
    }

    public static void refreshLocationObjectiveTracking(Player player, PlayerData data) {
        QuestLocationService.refreshTracking(player, data);
    }

    public static void tryStartQuest(Player player, PlayerData data, Quest quest, String npcId) {
        if (data.quests.stream().anyMatch(p -> p.questId.equals(quest.id))) {
            return;
        }

        if (quest.steps.isEmpty()) return;

        QuestStep firstStep = quest.steps.getFirst();
        if (!npcId.equals(firstStep.startNpc)) return;
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

        if (data.level < firstStep.requiredLevel) {
            ToastManager.showToast(player, Component.text("Niveau requis : " + firstStep.requiredLevel), Material.BARRIER, FrameType.TASK);
            return;
        }

        QuestProgress newProgress = new QuestProgress(quest.id);
        data.quests.add(newProgress);
        if (advanceToStep(player, data, quest, newProgress, 0)) {
            EVENT_NODE.call(new QuestStartEvent(player, quest));
        }
    }

    public static boolean tryAutoStartQuest(Player player, PlayerData data, Quest quest) {
        if (quest.steps.isEmpty()) {
            return false;
        }
        QuestStep firstStep = quest.steps.getFirst();
        if (firstStep.startNpc != null && !firstStep.startNpc.isEmpty()) {
            return false;
        }

        if (data.quests.stream().anyMatch(p -> p.questId.equals(quest.id))) return false;
        if (data.hasFailedQuest(quest.id)) return false;
        if (data.hasCompletedQuest(quest.id) && !quest.repeatable) return false;
        if (data.level < firstStep.requiredLevel) return false;
        if (!checkPrerequisites(data, firstStep)) return false;

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
            if (progress.stepCompletionTime == 0L) {
                progress.stepCompletionTime = System.currentTimeMillis();
            }
            advanceToStep(player, data, quest, progress, progress.stepIndex + 1);
        } else {
            showNpcDialogue(player, npcId, quest, currentStep.waitingDialogues);
        }
    }

    public static boolean advanceToStep(Player player, PlayerData data, Quest quest, QuestProgress progress, int newStepIndex) {
        boolean advancing = newStepIndex > progress.stepIndex;
        QuestStep oldStep = null;
        if (advancing && progress.stepIndex < quest.steps.size()) {
            oldStep = quest.steps.get(progress.stepIndex);
        }

        long now = System.currentTimeMillis();
        if (advancing && progress.stepCompletionTime == 0L) {
            progress.stepCompletionTime = now;
        }

        if (newStepIndex >= quest.steps.size()) {
            if (oldStep != null) {
                finalizeCompletedStep(player, data, quest, progress, oldStep);
            }
            ToastManager.showToast(player, quest.name, Material.CHEST, FrameType.CHALLENGE);
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER, 0.8f, 1f), player.getPosition());
            data.quests.removeIf(p -> p.questId.equals(quest.id));
            if (quest.repeatable) {
                data.questCooldowns.put(quest.id, now);
            } else if (!data.hasCompletedQuest(quest.id)) {
                data.completedQuests.add(quest.id);
            }
            EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
            QuestRegistry.autoStartQuests().forEach(q -> tryAutoStartQuest(player, data, q));
            return true;
        }

        QuestStep newStep = quest.steps.get(newStepIndex);
        if (oldStep != null) {
            finalizeCompletedStep(player, data, quest, progress, oldStep);
        }
        if (data.level < newStep.requiredLevel) {
            ToastManager.showToast(player, Component.text("Niveau requis : " + newStep.requiredLevel), Material.BARRIER, FrameType.TASK);
            return false;
        }
        if (!checkPrerequisites(data, newStep)) {
            ToastManager.showToast(player, Component.text("Vous ne remplissez pas les conditions."), Material.BARRIER, FrameType.TASK);
            return false;
        }

        if (advancing && !newStep.delay.isZero()) {
            long completionTime = progress.stepCompletionTime != 0L ? progress.stepCompletionTime : now;
            long timeSinceCompletion = now - completionTime;
            if (timeSinceCompletion < newStep.delay.toMillis()) {
                showNpcDialogue(player, newStep.startNpc, quest, newStep.delayDialogues);
                return false;
            }
        }

        progress.stepIndex = newStepIndex;
        progress.stepStartTime = now;
        progress.stepCompletionTime = 0L;
        progress.attempts = 1;
        progress.stepFinalized = false;
        progress.resetObjectiveCompletionStatus();

        if (newStepIndex == 0) {
            ToastManager.showToast(player, quest.name, Material.BOOK, FrameType.TASK);
        }
        if (newStep.description != null) {
            showNpcDialogue(player, newStep.startNpc, quest, List.of(newStep.description));
        }

        newStep.objectives.forEach(obj -> obj.onStart(player, data));
        refreshLocationObjectiveTracking(player, data);

        EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
        QuestRegistry.autoStartQuests().forEach(q -> tryAutoStartQuest(player, data, q));
        return true;
    }

    private static void finalizeCompletedStep(Player player, PlayerData data, Quest quest, QuestProgress progress, QuestStep step) {
        if (progress.stepFinalized) {
            return;
        }

        if (!step.successDialogues.isEmpty()) {
            NPC dialogueNpc = null;
            if (step.endNpc != null) {
                dialogueNpc = NpcRegistry.byId(step.endNpc);
            }
            if (dialogueNpc == null && step.startNpc != null) {
                dialogueNpc = NpcRegistry.byId(step.startNpc);
            }
            Component title = dialogueNpc != null ? dialogueNpc.name() : quest.name;
            NpcDialogService.showNarration(player, title, step.successDialogues);
        }

        for (IQuestObjective objective : step.objectives) {
            if (objective instanceof FetchObjective fetchObj) {
                TKit.removeItems(player, fetchObj.getItemToFetch().toItemStack(), fetchObj.getRequiredAmount());
            } else if (objective instanceof KillObjective killObj) {
                data.questCounters.remove(killObj.getProgressId());
            } else if (objective instanceof SlayObjective slayObj) {
                data.questCounters.remove(slayObj.getProgressId());
            }
            objective.onComplete(player, data);
        }

        step.rewards.forEach(reward -> reward.apply(player));
        QuestLocationService.untrack(player);

        // Preserve completion timestamp so delay checks can succeed.
        if (progress.stepCompletionTime == 0L) {
            progress.stepCompletionTime = System.currentTimeMillis();
        }
        progress.stepFinalized = true;
    }

    public static void handleQuestFailure(Player player, PlayerData data, Quest quest, QuestProgress progress) {
        QuestStep currentStep = quest.steps.get(progress.stepIndex);
        showNpcDialogue(player, currentStep.startNpc, quest, currentStep.failureDialogues);
        progress.attempts++;

        if (currentStep.attemptLimit > 0 && progress.attempts >= currentStep.attemptLimit) {
            data.quests.remove(progress);
            data.failedQuests.add(quest.id);
            QuestLocationService.untrack(player);
            currentStep.objectives.forEach(obj -> obj.onComplete(player, data));
            EVENT_NODE.call(new QuestFailEvent(player, quest));
            handleFailureRedirection(player, data, currentStep);
        } else {
            currentStep.objectives.forEach(obj -> obj.onReset(player, data));
            progress.stepStartTime = System.currentTimeMillis();
            progress.stepCompletionTime = 0L;
            progress.stepFinalized = false;
            progress.resetObjectiveCompletionStatus();
            refreshLocationObjectiveTracking(player, data);
        }
    }

    private static void showNpcDialogue(Player player, String npcId, Quest quest, List<Component> lines) {
        NPC npc = npcId != null ? NpcRegistry.byId(npcId) : null;
        Component title = npc != null ? npc.name()
                : quest != null ? quest.name
                : Component.text("Dialogue", NamedTextColor.GOLD);
        NpcDialogService.showNarration(player, title, lines);
    }

    public static boolean checkPrerequisites(PlayerData data, QuestStep step) {
        if (step.prerequisites == null || step.prerequisites.isEmpty()) return true;
        for (String prereq : step.prerequisites) {
            if (prereq == null || prereq.isBlank()) {
                return false;
            }

            String[] parts = prereq.split(":", 2);
            String requiredQuestId = parts[0].trim();
            if (requiredQuestId.isEmpty()) {
                return false;
            }

            if (parts.length > 1 && !parts[1].isBlank()) {
                try {
                    int requiredStepNumber = Integer.parseInt(parts[1].trim());
                    if (!data.hasReachedQuestStep(requiredQuestId, requiredStepNumber)) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if (!data.hasCompletedQuest(requiredQuestId)) {
                return false;
            }
        }
        return true;
    }

    private static void handleFailureRedirection(Player player, PlayerData data, QuestStep failedStep) {
        if (!failedStep.failureRedirection) {
            return;
        }
        if (failedStep.failureRedirectionQuest == null || failedStep.failureRedirectionQuest.isBlank()) {
            return;
        }

        Quest redirect = QuestRegistry.byId(failedStep.failureRedirectionQuest);
        if (redirect == null) {
            player.sendMessage(Component.text("Quête de secours introuvable : " + failedStep.failureRedirectionQuest, NamedTextColor.RED));
            return;
        }

        boolean started = false;
        if (!redirect.steps.isEmpty()) {
            QuestStep firstStep = redirect.steps.getFirst();
            if (firstStep.startNpc == null || firstStep.startNpc.isBlank()) {
                started = tryAutoStartQuest(player, data, redirect);
            }
        }

        if (!started) {
            Component toastText = Component.text("Nouvelle quête disponible : ", NamedTextColor.GOLD).append(redirect.name);
            ToastManager.showToast(player, toastText, Material.BOOK, FrameType.TASK);

            if (!redirect.steps.isEmpty()) {
                QuestStep first = redirect.steps.getFirst();
                if (first.startNpc != null && !first.startNpc.isBlank()) {
                    NPC npc = NpcRegistry.byId(first.startNpc);
                    if (npc != null) {
                        Component message = Component.text("Parlez à ", NamedTextColor.YELLOW)
                                .append(npc.name())
                                .append(Component.text(" pour poursuivre votre aventure.", NamedTextColor.YELLOW));
                        player.sendMessage(message);
                    }
                }
            }
        }
    }
}

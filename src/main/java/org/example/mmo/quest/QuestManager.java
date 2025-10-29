package org.example.mmo.quest;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.example.utils.TKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestManager {

    public static final Tag<String> NPC_ID_TAG = Tag.String("quest_npc_id");
    public static final Set<Player> playersWithLocationObjectives = ConcurrentHashMap.newKeySet();
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

    public static Set<Player> getPlayersWithLocationObjectives() {
        return playersWithLocationObjectives;
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

        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) return;

        NPC npc = NpcRegistry.byId(npcId);
        if (npc == null) return;

        player.playSound(npc.soundEffect(), player.getPosition());

        player.sendMessage(Component.text("--- " + TKit.extractPlainText(npc.name()) + " ---", NamedTextColor.GOLD));

        if (!npc.randomDialogues().isEmpty()) {
            String command = String.format("/npc_interact talk %s", npcId);
            Component hoverText = Component.text("Clique pour parler avec ", NamedTextColor.GRAY).append(npc.name());
            player.sendMessage(Component.text("[Parler]", NamedTextColor.WHITE, TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(command))
                    .hoverEvent(HoverEvent.showText(hoverText)));
        }

        List<QuestProgress> talkObjectivesToComplete = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest != null && progress.stepIndex < quest.steps.size()) {
                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                for (IQuestObjective objective : currentStep.objectives) {
                    if (objective instanceof TalkObjective talkObj && talkObj.getNpcId().equals(npcId) && !progress.isObjectiveCompleted(talkObj)) {
                        talkObjectivesToComplete.add(progress);
                        break;
                    }
                }
            }
        }

        if (!talkObjectivesToComplete.isEmpty()) {
            player.sendMessage(Component.text("Dialogue de quêtes :", NamedTextColor.YELLOW));
            for (QuestProgress progress : talkObjectivesToComplete) {
                Quest quest = QuestRegistry.byId(progress.questId);
                if (quest == null) continue;
                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                TalkObjective talkObj = (TalkObjective) currentStep.objectives.stream()
                        .filter(o -> o instanceof TalkObjective && ((TalkObjective) o).getNpcId().equals(npcId))
                        .findFirst().orElse(null);
                if (talkObj == null) continue;

                String command = String.format("/npc_interact talk_objective %s %s", npcId, quest.id);
                Component hoverText = talkObj.getDescription().color(NamedTextColor.GRAY);
                player.sendMessage(Component.text("  - " + TKit.extractPlainText(quest.name) + " (Dialogue)", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand(command))
                        .hoverEvent(HoverEvent.showText(hoverText)));
            }
        }

        List<Quest> questsToAdvance = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest != null && progress.stepIndex < quest.steps.size()) {
                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                if (npcId.equals(currentStep.endNpc)) {
                    if (currentStep.objectives.stream().allMatch(obj -> progress.isObjectiveCompleted(obj))) {
                        questsToAdvance.add(quest);
                    }
                }
            }
        }

        if (!questsToAdvance.isEmpty()) {
            player.sendMessage(Component.text("Quêtes en cours (cliquez pour valider) :", NamedTextColor.YELLOW));
            for (Quest quest : questsToAdvance) {
                String command = String.format("/npc_interact advance_quest %s %s", npcId, quest.id);
                QuestStep currentStep = quest.steps.get(data.quests.stream().filter(p -> p.questId.equals(quest.id)).findFirst().get().stepIndex);
                Component hoverText = currentStep.description.color(NamedTextColor.GRAY);
                player.sendMessage(Component.text("  - " + TKit.extractPlainText(quest.name), NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand(command))
                        .hoverEvent(HoverEvent.showText(hoverText)));
            }
        }

        List<Quest> availableQuests = new ArrayList<>();
        QuestRegistry.all().forEach((questId, quest) -> {
            boolean hasQuest = data.quests.stream().anyMatch(p -> p.questId.equals(questId));
            boolean hasCompleted = data.hasCompletedQuest(questId);
            boolean hasFailed = data.hasFailedQuest(questId);

            if (!hasQuest && !hasCompleted && !hasFailed && !quest.steps.isEmpty()) {
                if (npcId.equals(quest.steps.getFirst().startNpc) && checkPrerequisites(data, quest.steps.getFirst())) {
                    availableQuests.add(quest);
                }
            }
        });

        if (!availableQuests.isEmpty()) {
            player.sendMessage(Component.text("Quêtes disponibles :", NamedTextColor.YELLOW));
            for (Quest quest : availableQuests) {
                String command = String.format("/npc_interact start_quest %s %s", npcId, quest.id);
                Component hoverText = quest.steps.getFirst().description.color(NamedTextColor.GRAY);
                player.sendMessage(Component.text("  - " + TKit.extractPlainText(quest.name), NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand(command))
                        .hoverEvent(HoverEvent.showText(hoverText)));
            }
        }
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
                    long remainingSeconds = (quest.cooldown.toMillis() - timeSinceCompletion) / 1000;
                    player.sendMessage(Component.text("Vous devez encore attendre " + TKit.formatTime((int) remainingSeconds) + " minutes.", NamedTextColor.YELLOW));
                    return;
                }
            }
        } else if (data.hasCompletedQuest(quest.id)) {
            return;
        }

        if (data.level < quest.requiredLevel) {
            player.sendMessage(Component.text("Vous n'avez pas le niveau requis.", NamedTextColor.RED));
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
            currentStep.waitingDialogues.forEach(player::sendMessage);
        }
    }

    private static boolean advanceToStep(Player player, PlayerData data, Quest quest, QuestProgress progress, int newStepIndex) {
        if (newStepIndex > 0 && progress.stepIndex < quest.steps.size()) {
            QuestStep oldStep = quest.steps.get(progress.stepIndex);
            oldStep.successDialogues.forEach(player::sendMessage);

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
            playersWithLocationObjectives.remove(player);
            progress.resetObjectiveCompletionStatus();
        }

        if (newStepIndex >= quest.steps.size()) {
            player.sendMessage(Component.text("Quête terminée : ", NamedTextColor.GREEN).append(quest.name.color(NamedTextColor.GOLD)));
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER, 1f, 1f), player.getPosition());
            data.quests.removeIf(p -> p.questId.equals(quest.id));
            if (quest.repeatable) {
                data.questCooldowns.put(quest.id, System.currentTimeMillis());
            } else if (!data.hasCompletedQuest(quest.id)) {
                data.completedQuests.add(quest.id);
            }
            EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
            QuestRegistry.all().values().forEach(q -> tryAutoStartQuest(player, data, q));
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
        progress.resetObjectiveCompletionStatus();

        if (newStepIndex == 0) {
            player.sendMessage(Component.text("Nouvelle quête : ", NamedTextColor.GREEN).append(quest.name.color(NamedTextColor.GOLD)));
        } else {
            Component stepMessage = Component.text("Nouvel objectif (", NamedTextColor.YELLOW)
                    .append(quest.name.color(NamedTextColor.GOLD))
                    .append(Component.text(") : ", NamedTextColor.YELLOW))
                    .append(newStep.name != null ? newStep.name : Component.empty());
            player.sendMessage(stepMessage);
        }
        player.sendMessage(newStep.description.color(NamedTextColor.WHITE));

        newStep.objectives.forEach(obj -> {
            obj.onStart(player, data);
            if (obj instanceof LocationObjective) playersWithLocationObjectives.add(player);
        });

        EVENT_NODE.call(new QuestStepAdvanceEvent(player, quest, newStepIndex));
        QuestRegistry.all().values().forEach(q -> tryAutoStartQuest(player, data, q));
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
            currentStep.objectives.forEach(obj -> obj.onComplete(player, data));
            EVENT_NODE.call(new QuestFailEvent(player, quest));
        } else {
            player.sendMessage(Component.text("Vous pouvez réessayer.", NamedTextColor.YELLOW));
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
        int currentCount = data.getQuestCounter(progressId);

        Component feedback = Component.text("(", NamedTextColor.GRAY)
                .append(quest.name.color(NamedTextColor.GOLD))
                .append(Component.text(") ", NamedTextColor.GRAY))
                .append(objective.getDescription().color(NamedTextColor.WHITE))
                .append(Component.text(": " + currentCount + "/" + requiredCount, NamedTextColor.GRAY));
        player.sendMessage(feedback);

        boolean isNowComplete = currentCount >= requiredCount;
        if (isNowComplete) {
            progress.setObjectiveCompleted(objective, true);
        }

        EVENT_NODE.call(new QuestObjectiveProgressEvent(player));

        return isNowComplete;
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

        event.getCompletedObjective().onComplete(player, data);

        player.sendMessage(Component.text("Objectif atteint : ", NamedTextColor.GREEN).append(event.getCompletedObjective().getDescription()));

        boolean allComplete = completedStep.objectives.stream().allMatch(obj -> event.getQuestProgress().isObjectiveCompleted(obj));

        if (allComplete) {
            if (completedStep.endNpc == null || completedStep.endNpc.isEmpty()) {
                Quest quest = QuestRegistry.byId(event.getQuestProgress().questId);
                advanceToStep(player, data, quest, event.getQuestProgress(), event.getQuestProgress().stepIndex + 1);
            } else {
                NPC endNpc = NpcRegistry.byId(completedStep.endNpc);
                if (endNpc != null) {
                    player.sendMessage(Component.text("Tous les objectifs sont remplis. Retournez voir ", NamedTextColor.YELLOW).append(endNpc.name()));
                }
            }
        }
    }
}

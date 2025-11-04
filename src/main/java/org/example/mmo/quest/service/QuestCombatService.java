package org.example.mmo.quest.service;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.combat.history.DamageHistory;
import org.example.mmo.combat.history.DamageRecord;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.PlayerKillEntityEvent;
import org.example.mmo.quest.event.QuestObjectiveCompleteEvent;
import org.example.mmo.quest.event.QuestObjectiveProgressEvent;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.SlayObjective;
import org.example.mmo.npc.mob.MobMetadataKeys;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

import java.util.ArrayList;

public final class QuestCombatService {

    private QuestCombatService() {
    }

    public static void register(EventNode<Event> eventNode) {
        eventNode.addListener(EntityDeathEvent.class, QuestCombatService::handleEntityDeath);
        eventNode.addListener(PlayerKillEntityEvent.class, QuestCombatService::handlePlayerKill);
    }

    private static void handleEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity deadEntity) {
            DamageHistory history = DamageTracker.getHistory(deadEntity);
            if (history != null) {
                Player killer = history.findLastPlayerAttacker();
                if (killer != null) {
                    QuestManager.getEventNode().call(new PlayerKillEntityEvent(killer, deadEntity));
                }
            }
            DamageTracker.clear(deadEntity);
        }
    }

    private static void handlePlayerKill(PlayerKillEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData data = GameContext.get().playerDataService().get(player);
        if (data == null) {
            return;
        }

        String mobId = event.getKilled().getTag(MobMetadataKeys.ARCHETYPE_ID);
        if (mobId == null || mobId.isBlank()) {
            return;
        }

        for (QuestProgress progress : new ArrayList<>(data.quests)) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) {
                continue;
            }

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof KillObjective killObj && mobId.equals(killObj.getMobId())) {
                    if (tryIncrementObjective(player, data, quest, progress, currentStep, killObj, killObj.getProgressId(), killObj.getCount())) {
                        QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(player, progress, currentStep, killObj));
                    }
                } else if (objective instanceof SlayObjective slayObj && mobId.equals(slayObj.getMobId())) {
                    DamageHistory history = DamageTracker.getHistory(event.getKilled());
                    if (history != null) {
                        DamageRecord lastDamage = history.getLastDamage();
                        if (lastDamage != null && slayObj.getKillCondition().test(lastDamage.damage())) {
                            if (tryIncrementObjective(player, data, quest, progress, currentStep, slayObj, slayObj.getProgressId(), slayObj.getCount())) {
                                QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(player, progress, currentStep, slayObj));
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean tryIncrementObjective(Player player,
                                                 PlayerData data,
                                                 Quest quest,
                                                 QuestProgress progress,
                                                 QuestStep step,
                                                 IQuestObjective objective,
                                                 String progressId,
                                                 int requiredCount) {
        if (progress.isObjectiveCompleted(objective)) {
            return false;
        }

        data.incrementQuestCounter(progressId, 1);

        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.NEUTRAL, 0.5f, 1.2f), player.getPosition());

        boolean isNowComplete = data.getQuestCounter(progressId) >= requiredCount;
        if (isNowComplete) {
            progress.setObjectiveCompleted(objective, true);
        }

        QuestManager.getEventNode().call(new QuestObjectiveProgressEvent(player));

        return isNowComplete;
    }
}

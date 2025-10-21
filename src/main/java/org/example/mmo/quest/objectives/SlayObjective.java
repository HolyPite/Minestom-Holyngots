package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.api.IQuestObjective;

import java.util.function.Predicate;

/**
 * An objective that requires the player to kill a certain number of entities
 * under specific conditions, tested against the final killing blow.
 */
public class SlayObjective implements IQuestObjective {

    private final EntityType entityType;
    private final int count;
    private final String progressId;
    private final Component description;
    private final Predicate<Damage> killCondition;

    public SlayObjective(EntityType entityType, int count, String progressId, Component description, Predicate<Damage> killCondition) {
        this.entityType = entityType;
        this.count = count;
        this.progressId = progressId;
        this.description = description;
        this.killCondition = killCondition;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getProgressId() {
        return progressId;
    }

    public int getCount() {
        return count;
    }

    public Predicate<Damage> getKillCondition() {
        return killCondition;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        return data.getQuestCounter(progressId) >= count;
    }

    @Override
    public void onStart(Player player, PlayerData data) {
        data.setQuestCounter(progressId, 0);
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        // The counter can be left as is, or reset if you want the objective to be repeatable in the future
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        data.setQuestCounter(progressId, 0);
    }
}

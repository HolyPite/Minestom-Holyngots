package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.IQuestObjective;

/**
 * An objective that requires the player to reach a specific location.
 */
public class LocationObjective implements IQuestObjective {

    private final Pos center;
    private final double radius;
    private final Component description;

    public LocationObjective(Pos center, double radius, Component description) {
        this.center = center;
        this.radius = radius;
        this.description = description;
    }

    public Pos getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        return player.getPosition().distanceSquared(center) <= radius * radius;
    }

    @Override
    public void onStart(Player player, PlayerData data) {
        // No special action needed when the objective starts.
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        // No special action needed upon completion, as it's a passive objective.
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        // No state to reset for this objective type.
    }
}

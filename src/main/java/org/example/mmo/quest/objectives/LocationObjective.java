package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.utils.TKit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocationObjective implements IQuestObjective {

    private static final Map<UUID, Map<String, Task>> PLAYER_PARTICLE_TASKS = new ConcurrentHashMap<>();

    private final Pos center;
    private final double radius;
    private final Component description;
    private final String objectiveId;

    public LocationObjective(Pos center, double radius, Component description) {
        this.center = center;
        this.radius = radius;
        this.description = description;
        this.objectiveId = String.format("loc_%.0f_%.0f_%.0f", center.x(), center.y(), center.z());
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
        PLAYER_PARTICLE_TASKS.computeIfAbsent(player.getUuid(), k -> new ConcurrentHashMap<>());

        Task particleTask = player.scheduler().buildTask(() -> {
            Pos particlePos = center.add(0, 10, 0);
            TKit.spawnParticles(player.getInstance(), Particle.END_ROD, particlePos, 0.5f, 2f, 0.5f, 0.05f, 10);
        }).repeat(TaskSchedule.seconds(1)).schedule();

        PLAYER_PARTICLE_TASKS.get(player.getUuid()).put(this.objectiveId, particleTask);
        QuestManager.trackLocationObjective(player);
    }

    @Override
    public void onComplete(Player player, PlayerData data) {
        Map<String, Task> playerTasks = PLAYER_PARTICLE_TASKS.get(player.getUuid());
        if (playerTasks != null) {
            Task task = playerTasks.remove(this.objectiveId);
            if (task != null) {
                task.cancel();
            }
        }

        QuestManager.refreshLocationObjectiveTracking(player, data);
    }

    @Override
    public void onReset(Player player, PlayerData data) {
        onComplete(player, data);
    }
}

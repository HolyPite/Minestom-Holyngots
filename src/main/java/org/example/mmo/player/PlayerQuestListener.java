package org.example.mmo.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.event.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerQuestListener {

    private static final Map<UUID, QuestScoreboard> SCOREBOARDS = new ConcurrentHashMap<>();

    public static void init(EventNode<PlayerEvent> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            QuestScoreboard scoreboard = new QuestScoreboard(player);
            SCOREBOARDS.put(player.getUuid(), scoreboard);
            updateScoreboard(player);
        });

        eventNode.addListener(PlayerDisconnectEvent.class, event -> {
            SCOREBOARDS.remove(event.getPlayer().getUuid());
        });

        eventNode.addListener(QuestStartEvent.class, event -> updateScoreboard(event.getPlayer()));
        eventNode.addListener(QuestStepAdvanceEvent.class, event -> updateScoreboard(event.getPlayer()));
        eventNode.addListener(QuestObjectiveProgressEvent.class, event -> updateScoreboard(event.getPlayer()));
        eventNode.addListener(QuestObjectiveCompleteEvent.class, event -> updateScoreboard(event.getPlayer()));
        eventNode.addListener(QuestFailEvent.class, event -> updateScoreboard(event.getPlayer()));
    }

    private static void updateScoreboard(Player player) {
        QuestScoreboard scoreboard = SCOREBOARDS.get(player.getUuid());
        PlayerData data = GameContext.get().playerDataService().get(player);
        if (scoreboard != null && data != null) {
            scoreboard.update(data);
        }
    }
}

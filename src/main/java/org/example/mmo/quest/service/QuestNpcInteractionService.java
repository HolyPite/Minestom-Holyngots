package org.example.mmo.quest.service;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.npc.dialog.NpcDialogService;
import org.example.mmo.quest.QuestManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestNpcInteractionService {

    private static final Map<UUID, Long> LAST_INTERACTION_TIME = new ConcurrentHashMap<>();
    private static final long INTERACTION_COOLDOWN_MILLIS = 1_000L;

    private QuestNpcInteractionService() {
    }

    public static void register(EventNode<Event> eventNode) {
        eventNode.addListener(PlayerEntityInteractEvent.class, QuestNpcInteractionService::handleNpcInteraction);
    }

    private static void handleNpcInteraction(PlayerEntityInteractEvent event) {
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        long lastTime = LAST_INTERACTION_TIME.getOrDefault(player.getUuid(), 0L);

        if (now - lastTime < INTERACTION_COOLDOWN_MILLIS) {
            return;
        }
        LAST_INTERACTION_TIME.put(player.getUuid(), now);

        String npcId = event.getTarget().getTag(QuestManager.NPC_ID_TAG);
        if (npcId == null || npcId.isEmpty()) {
            return;
        }

        NPC npc = NpcRegistry.byId(npcId);
        if (npc == null) {
            return;
        }

        player.playSound(npc.soundEffect(), player.getPosition());
        NpcDialogService.openMainDialog(player, npc);
    }
}

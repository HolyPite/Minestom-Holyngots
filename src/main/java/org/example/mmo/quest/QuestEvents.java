package org.example.mmo.quest;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.utils.TKit;

public class QuestEvents {
    public static void init(EventNode<Event> events) {

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode", PlayerEvent.class).getFirst();
        EventNode<EntityEvent> entityNode = events.findChildren("entityNode", EntityEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode", InventoryEvent.class).getFirst();

        /* Interaction with entities ------------------------------------- */
        playerNode.addListener(PlayerEntityInteractEvent.class, e -> {
            Entity target = e.getTarget();
            String id = target.getTag(QuestTags.ID);
            if (id != null && !id.isEmpty()) {
                Player player = e.getPlayer();
                PlayerData data = NodesManagement.getDataService().get(player);
                if (data == null) return;

                java.util.List<String> lines = new java.util.ArrayList<>();

                QuestRegistry.all().forEach((qid, quest) -> {
                    QuestProgress progress = data.quests.stream()
                            .filter(p -> p.questId.equals(qid))
                            .findFirst()
                            .orElse(null);

                    if (progress == null) {
                        if (!quest.steps.isEmpty() && id.equals(quest.steps.get(0).startNpc)) {
                            lines.add(qid + " : " + TKit.extractPlainText(quest.steps.get(0).description));
                        }
                        return;
                    }

                    if (progress.stepIndex < quest.steps.size()) {
                        QuestStep step = quest.steps.get(progress.stepIndex);
                        if (id.equals(step.startNpc) || id.equals(step.endNpc)) {
                            lines.add(qid + " : " + TKit.extractPlainText(step.description));
                        }
                    }
                });

                if (!lines.isEmpty()) {
                    player.sendMessage("=== Quêtes disponibles ===");
                    lines.forEach(line -> player.sendMessage("- " + line));
                }
            }

        });
    }
}

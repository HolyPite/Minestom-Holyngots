package org.example.mmo.quest;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;

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
                // TODO: display available quest steps when interacting with tagged NPCs
            }

        });
    }
}

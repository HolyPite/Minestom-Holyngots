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

        /* Inventory clic ------------------------------------------------ */
        playerNode.addListener(PlayerEntityInteractEvent.class, e -> {
            Entity target = e.getTarget();
            if (target.getTag("id") == "") {
                //Send in the player chat a component for each quest step available at this entity
                //The component as hovering saying click to accept/valid
                //If the player click on the component it accept the quest/check if the player as the ressources to valid the quest step
            }

        });
    }
}

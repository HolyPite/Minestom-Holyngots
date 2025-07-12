package org.example.mmo.combats;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;

public class CombatListener {
        public static void init(EventNode<Event> events){

                EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();
                EventNode<EntityEvent> entityNode = events.findChildren("entityNode",EntityEvent.class).getFirst();
                EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode",InventoryEvent.class).getFirst();

                entityNode.addListener(net.minestom.server.event.entity.EntityAttackEvent.class, evt -> {
                        Entity damager = evt.getEntity();
                        Entity target  = evt.getTarget();
                        if (!(damager instanceof LivingEntity attacker) || !(target  instanceof LivingEntity victim)) return;
                        CombatEngine.computeDamage(attacker, victim);
                        victim.damage(Damage.fromEntity(attacker,0));
            });
        };
}

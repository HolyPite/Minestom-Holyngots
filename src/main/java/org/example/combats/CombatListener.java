package org.example.combats;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;

public class CombatListener {
        public static void init(EventNode<InstanceEvent> events){
                events.addListener(net.minestom.server.event.entity.EntityAttackEvent.class, evt -> {
                        Entity damager = evt.getEntity();
                        Entity target  = evt.getTarget();
                        if (!(damager instanceof LivingEntity attacker) || !(target  instanceof LivingEntity victim)) return;
                        CombatEngine.computeDamage(attacker, victim);
                        victim.damage(Damage.fromEntity(attacker,0));
            });
        };
}

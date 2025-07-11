package org.example.combats;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.GlobalEventHandler;

public class CombatListener {
        public static void init(GlobalEventHandler events){
                events.addListener(net.minestom.server.event.entity.EntityAttackEvent.class, evt -> {
                        Entity damager = evt.getEntity();
                        Entity target  = evt.getTarget();
                        if (!(damager instanceof LivingEntity attacker) || !(target  instanceof LivingEntity victim)) return;
                        if (CombatEngine.computeDamage(attacker, victim) != 0) victim.damage(Damage.fromEntity(attacker,0));
            });
        };
}

package org.example.mmo.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;

public class CombatListener {
    public static void init(EventNode<Event> events) {

        EventNode<EntityEvent> entityNode = events.findChildren("entityNode", EntityEvent.class).getFirst();

        entityNode.addListener(EntityAttackEvent.class, evt -> {
            Entity damager = evt.getEntity();
            Entity target = evt.getTarget();
            if (!(damager instanceof LivingEntity attacker) || !(target instanceof LivingEntity victim)) return;

            // Your custom damage calculation remains untouched
            CombatEngine.computeDamage(attacker, victim);

            // Create the damage object that will be used for both applying and recording
            Damage damage = Damage.fromEntity(attacker, 0); // Amount is 0 as per your original logic

            // Apply the damage to trigger animations and vanilla events
            victim.damage(damage);

            // Record the damage event in our new tracker system
            DamageTracker.recordDamage(victim, damage);
        });
    }
}

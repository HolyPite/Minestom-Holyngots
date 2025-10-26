package org.example.mmo.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.combat.util.CombatFeedback;
import org.example.mmo.combat.util.HealthUtils;
import org.example.mmo.combat.util.StatUtils;
import org.example.mmo.item.datas.StatType;

public class CombatListener {
    public static void init(EventNode<Event> events) {

        EventNode<EntityEvent> entityNode = events.findChildren("entityNode", EntityEvent.class).getFirst();

        entityNode.addListener(EntityAttackEvent.class, evt -> {
            Entity damager = evt.getEntity();
            Entity target = evt.getTarget();

            if (!(damager instanceof LivingEntity attacker) || !(target instanceof LivingEntity victim)) return;

            // --- 1. Invulnerability Check (Fixes NPC health bar bug) ---
            if (victim.isInvulnerable()) {
                return; // Do not process combat logic for invulnerable entities
            }

            // --- 2. Calculate Damage ---
            double damageAmount = CombatEngine.computeDamage(attacker, victim);

            // --- 3. Create and Record Damage (Fixes One-Shot Bug) ---
            Damage damage = Damage.fromEntity(attacker, (float) damageAmount);
            DamageTracker.recordDamage(victim, damage);

            // --- 4. Apply Side Effects (Lifesteal, Stun, etc.) ---
            double lifesteal = StatUtils.getTotal(attacker, StatType.LIFESTEAL) / 100.0;
            if (lifesteal > 0) {
                double heal = damageAmount * lifesteal;
                HealthUtils.heal(attacker, heal);
                CombatFeedback.showHeal(attacker, heal);
            }

            // --- 5. Apply Damage and Visual Feedback ---
            victim.damage(damage);
            CombatFeedback.showHit(victim);
            if (!(victim instanceof Player)) {
                HealthUtils.updateHealthBar(victim);
            }
        });
    }
}

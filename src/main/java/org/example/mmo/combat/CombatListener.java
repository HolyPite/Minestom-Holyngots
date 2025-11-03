package org.example.mmo.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.combat.ui.FloatingCombatText;
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

            if (!(attacker instanceof Player) && !(victim instanceof Player)) {
                return;
            }

            // --- 1. Invulnerability Check (Fixes NPC health bar bug) ---
            if (victim.isInvulnerable()) {
                return; // Do not process combat logic for invulnerable entities
            }

            // --- 2. Calculate Damage ---
            Damage damage = CombatEngine.buildDamage(attacker, victim);

            // --- 4. Apply Damage and Visual Feedback ---
            victim.damage(damage);
        });

        entityNode.addListener(EntityDamageEvent.class, event -> {
            if (!(event.getEntity() instanceof LivingEntity victim)) {
                return;
            }
            Damage damage = event.getDamage();
            if (damage == null || damage.getAmount() <= 0f || event.isCancelled()) {
                return;
            }
            DamageTracker.recordDamage(victim, damage);
            Entity attackerEntity = damage.getAttacker();

            if (damage.getAmount() > 0f) {
                float maxHealth = HealthUtils.resolveMaxHealth(victim);
                float currentHealth = Math.max(0f, victim.getHealth());
                LivingEntity livingAttacker = attackerEntity instanceof LivingEntity living ? living : null;
                CombatFeedback.showHit(livingAttacker, victim, damage.getAmount(), currentHealth, maxHealth, damage.getType());
                FloatingCombatText.showDamage(victim, damage.getAmount(), damage.getType());
                if (!(victim instanceof Player)) {
                    HealthUtils.updateHealthBar(victim);
                }
            }

            if (attackerEntity instanceof LivingEntity attacker) {
                double lifesteal = StatUtils.getTotal(attacker, StatType.LIFESTEAL) / 100.0;
                if (lifesteal > 0) {
                    double heal = damage.getAmount() * lifesteal;
                    if (heal > 0) {
                        HealthUtils.heal(attacker, heal);
                        CombatFeedback.showHeal(attacker, heal);
                        FloatingCombatText.showHeal(attacker, heal);
                    }
                }
            }
        });
    }
}

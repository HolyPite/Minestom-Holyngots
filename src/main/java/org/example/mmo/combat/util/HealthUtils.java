package org.example.mmo.combat.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import org.example.mmo.item.datas.StatType;

public class HealthUtils {
    /**
     * Applies damage to a victim, ensuring that Minestom's internal death logic is triggered correctly.
     * @param attacker The entity that initiated the damage.
     * @param victim The entity receiving the damage.
     * @param amount The amount of damage to apply.
     */
    public static void damage(LivingEntity attacker, LivingEntity victim, double amount) {
        // Using victim.damage() is crucial as it triggers the EntityDeathEvent with the correct source.
        // setHealth() is too direct and bypasses this logic, causing issues with one-shot kills.
        victim.damage(Damage.fromEntity(attacker, (float) amount));
    }

    public static void heal(LivingEntity target, double amount) {
        float maxHealth = StatUtils.getTotal(target, StatType.HEALTH);
        target.setHealth((float) Math.min(maxHealth, target.getHealth() + amount));
    }

    public static void updateHealthBar(LivingEntity victim) {
        if (victim instanceof Player) return;
        float maxHealth = StatUtils.getTotal(victim, StatType.HEALTH);
        if (maxHealth <= 0) return; // Avoid division by zero

        double ratio = victim.getHealth() / maxHealth;
        String name = "HP: " + Math.round(victim.getHealth()) + " / " + Math.round(maxHealth);
        
        victim.setCustomName(Component.text(name));
        victim.setCustomNameVisible(ratio < 0.99);
    }
}

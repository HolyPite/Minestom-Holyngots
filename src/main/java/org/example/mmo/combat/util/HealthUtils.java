package org.example.mmo.combat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.mob.MobMetadataKeys;

public final class HealthUtils {

    private HealthUtils() {
    }

    /**
     * Applies damage to a victim, ensuring that Minestom's internal death logic is triggered correctly.
     * @param attacker The entity that initiated the damage.
     * @param victim The entity receiving the damage.
     * @param amount The amount of damage to apply.
     */
    public static void damage(LivingEntity attacker, LivingEntity victim, double amount) {
        victim.damage(Damage.fromEntity(attacker, (float) amount));
    }

    public static void heal(LivingEntity target, double amount) {
        float maxHealth = resolveMaxHealth(target);
        target.setHealth((float) Math.min(maxHealth, target.getHealth() + amount));
        if (!(target instanceof Player)) {
            updateHealthBar(target);
        }
    }

    public static void updateHealthBar(LivingEntity victim) {
        if (victim instanceof Player) {
            return;
        }
        float maxHealth = resolveMaxHealth(victim);
        if (maxHealth <= 0f) {
            return;
        }
        float currentHealth = Math.max(0f, victim.getHealth());
        Component display = buildHealthComponent(victim, currentHealth, maxHealth);
        victim.setCustomName(display);
        victim.setCustomNameVisible(true);
    }

    public static Component buildHealthComponent(LivingEntity entity, float health, float maxHealth) {
        Component base = baseDisplayName(entity);
        int roundedHealth = Math.round(health);
        int roundedMax = Math.round(maxHealth);
        return Component.text()
                .append(base)
                .append(Component.text(" ", NamedTextColor.WHITE))
                .append(Component.text(roundedHealth + "/" + roundedMax + " PV", NamedTextColor.GRAY))
                .build();
    }

    public static Component baseDisplayName(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getName();
        }
        Component tagComponent = entity.getTag(MobMetadataKeys.DISPLAY_NAME);
        if (tagComponent != null) {
            return tagComponent;
        }
        Component base = entity.getCustomName();
        if (base == null) {
            String fallback = entity.getEntityType().name().replace('_', ' ');
            base = Component.text(fallback, NamedTextColor.WHITE);
        }
        entity.setTag(MobMetadataKeys.DISPLAY_NAME, base);
        return base;
    }

    public static float resolveMaxHealth(LivingEntity entity) {
        float maxHealth = StatUtils.getTotal(entity, StatType.HEALTH);
        if (maxHealth > 0f) {
            return maxHealth;
        }
        var attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            return (float) attribute.getValue();
        }
        return Math.max(entity.getHealth(), 1f);
    }
}


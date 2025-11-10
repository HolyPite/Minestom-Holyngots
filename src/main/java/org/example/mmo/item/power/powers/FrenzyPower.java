package org.example.mmo.item.power.powers;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.example.mmo.combat.util.HealthUtils;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;

@PowerId(FrenzyPower.ID)
public final class FrenzyPower implements Power {

    public static final String ID = "core:frenzy_buff";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        LivingEntity entity = context.entity();
        if (entity == null) {
            return;
        }
        double threshold = clamp(parameters.get("threshold_ratio", 0.5), 0.0, 1.0);
        float maxHealth = HealthUtils.resolveMaxHealth(entity);
        if (maxHealth <= 0f) {
            return;
        }
        float ratio = entity.getHealth() / maxHealth;
        if (ratio > threshold) {
            return;
        }

        int duration = (int) Math.max(20, parameters.get("duration_ticks", 100));
        int strengthAmplifier = (int) parameters.get("strength_amplifier", 0);
        int speedAmplifier = (int) parameters.get("speed_amplifier", 0);

        if (strengthAmplifier >= 0) {
            entity.addEffect(new Potion(PotionEffect.STRENGTH, strengthAmplifier, duration));
        }
        if (speedAmplifier >= 0) {
            entity.addEffect(new Potion(PotionEffect.SPEED, speedAmplifier, duration));
        }
    }

    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}

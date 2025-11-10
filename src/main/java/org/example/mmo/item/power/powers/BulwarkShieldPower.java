package org.example.mmo.item.power.powers;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;

@PowerId(BulwarkShieldPower.ID)
public final class BulwarkShieldPower implements Power {

    public static final String ID = "core:bulwark_shield";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        LivingEntity entity = context.entity();
        if (entity == null) {
            return;
        }
        int amplifier = (int) Math.max(0, parameters.get("amplifier", 1));
        int duration = (int) Math.max(20, parameters.get("duration_ticks", 60));
        entity.addEffect(new Potion(PotionEffect.RESISTANCE, amplifier, duration));
    }
}

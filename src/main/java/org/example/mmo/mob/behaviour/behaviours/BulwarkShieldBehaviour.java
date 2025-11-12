package org.example.mmo.mob.behaviour.behaviours;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.example.mmo.mob.MobBehaviourAdapter;
import org.example.mmo.mob.MobInstance;

/**
 * Maintains a permanent resistance buff on the mob to emphasise its tank role.
 */
public final class BulwarkShieldBehaviour extends MobBehaviourAdapter {

    private static final int EFFECT_DURATION_TICKS = 20 * 20; // 20 seconds
    private static final int REFRESH_INTERVAL_TICKS = 20 * 5; // refresh every 5 seconds

    private final LivingEntity entity;
    private final byte amplifier;

    public BulwarkShieldBehaviour(LivingEntity entity, int amplifier) {
        this.entity = entity;
        this.amplifier = (byte) amplifier;
    }

    @Override
    public void onSpawn(MobInstance instance) {
        applyEffect();
    }

    @Override
    public void onTick(MobInstance instance, long tickTime) {
        if (tickTime % REFRESH_INTERVAL_TICKS == 0) {
            applyEffect();
        }
    }

    @Override
    public void onCleanup(MobInstance instance) {
        entity.removeEffect(PotionEffect.RESISTANCE);
    }

    private void applyEffect() {
        entity.addEffect(new Potion(PotionEffect.RESISTANCE, amplifier, EFFECT_DURATION_TICKS));
    }
}

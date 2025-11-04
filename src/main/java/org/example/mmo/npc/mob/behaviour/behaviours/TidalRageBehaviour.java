package org.example.mmo.npc.mob.behaviour.behaviours;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.example.mmo.npc.mob.MobBehaviourAdapter;
import org.example.mmo.npc.mob.MobInstance;

/**
 * Grants temporary offensive buffs when the mob acquires a target.
 */
public final class TidalRageBehaviour extends MobBehaviourAdapter {

    private static final int BUFF_DURATION_TICKS = 20 * 8; // 8 seconds
    private static final int COOLDOWN_TICKS = 20 * 20; // 20 seconds

    private final LivingEntity entity;
    private final byte strengthAmplifier;
    private final byte speedAmplifier;
    private long lastBuffTick = Long.MIN_VALUE;

    public TidalRageBehaviour(LivingEntity entity, int strengthAmplifier, int speedAmplifier) {
        this.entity = entity;
        this.strengthAmplifier = (byte) strengthAmplifier;
        this.speedAmplifier = (byte) speedAmplifier;
    }

    @Override
    public void onSpawn(MobInstance instance) {
        lastBuffTick = Long.MIN_VALUE;
    }

    @Override
    public void onAggro(MobInstance instance, Entity target) {
        long currentTick = entity.getAliveTicks();
        if (currentTick - lastBuffTick < COOLDOWN_TICKS) {
            return;
        }
        lastBuffTick = currentTick;
        entity.addEffect(new Potion(PotionEffect.STRENGTH, strengthAmplifier, BUFF_DURATION_TICKS));
        entity.addEffect(new Potion(PotionEffect.SPEED, speedAmplifier, BUFF_DURATION_TICKS));
    }

    @Override
    public void onCleanup(MobInstance instance) {
        entity.removeEffect(PotionEffect.STRENGTH);
        entity.removeEffect(PotionEffect.SPEED);
    }
}

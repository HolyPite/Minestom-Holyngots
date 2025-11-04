package org.example.mmo.npc.mob.behaviour.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.example.mmo.combat.util.HealthUtils;
import org.example.mmo.npc.mob.MobBehaviourAdapter;
import org.example.mmo.npc.mob.MobInstance;
import org.example.mmo.npc.mob.MobMetadataKeys;

/**
 * Demo behaviour that renames the mob once it drops below a health threshold.
 */
public final class EnrageOnLowHealthBehaviour extends MobBehaviourAdapter {

    private final LivingEntity entity;
    private final float thresholdRatio;
    private boolean enraged;
    private Component originalName;

    public EnrageOnLowHealthBehaviour(LivingEntity entity, float thresholdRatio) {
        this.entity = entity;
        this.thresholdRatio = thresholdRatio;
    }

    @Override
    public void onSpawn(MobInstance instance) {
        originalName = entity.getTag(MobMetadataKeys.DISPLAY_NAME);
        if (originalName == null) {
            originalName = HealthUtils.baseDisplayName(entity);
        }
        enraged = false;
    }

    @Override
    public void onDamaged(MobInstance instance, Damage damage) {
        if (enraged) {
            return;
        }
        float maxHealth = HealthUtils.resolveMaxHealth(entity);
        if (maxHealth <= 0f) {
            return;
        }
        double ratio = entity.getHealth() / maxHealth;
        if (ratio <= thresholdRatio) {
            enraged = true;
            Component enragedName = Component.text("Enrag\u00E9 ", NamedTextColor.DARK_RED).append(originalName);
            entity.setTag(MobMetadataKeys.DISPLAY_NAME, enragedName);
            HealthUtils.updateHealthBar(entity);
        }
    }

    @Override
    public void onCleanup(MobInstance instance) {
        if (originalName != null) {
            entity.setTag(MobMetadataKeys.DISPLAY_NAME, originalName);
            HealthUtils.updateHealthBar(entity);
        }
        enraged = false;
    }
}

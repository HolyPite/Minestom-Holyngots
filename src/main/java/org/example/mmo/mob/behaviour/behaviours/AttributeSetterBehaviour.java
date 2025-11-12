package org.example.mmo.mob.behaviour.behaviours;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import org.example.mmo.mob.MobBehaviourAdapter;
import org.example.mmo.mob.MobInstance;

/**
 * Behaviour that sets an attribute base value on spawn.
 */
public final class AttributeSetterBehaviour extends MobBehaviourAdapter {

    private final LivingEntity entity;
    private final Attribute attribute;
    private final double value;

    public AttributeSetterBehaviour(LivingEntity entity, Attribute attribute, double value) {
        this.entity = entity;
        this.attribute = attribute;
        this.value = value;
    }

    @Override
    public void onSpawn(MobInstance instance) {
        var attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(value);
        }
    }
}

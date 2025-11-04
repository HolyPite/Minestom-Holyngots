package org.example.mmo.npc.mob.behaviour.behaviours;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.example.mmo.npc.mob.MobBehaviourAdapter;
import org.example.mmo.npc.mob.MobInstance;

/**
 * Simple behaviour that equips an item on spawn.
 */
public final class EquipItemBehaviour extends MobBehaviourAdapter {

    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack itemStack;

    public EquipItemBehaviour(LivingEntity entity, EquipmentSlot slot, ItemStack itemStack) {
        this.entity = entity;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    @Override
    public void onSpawn(MobInstance instance) {
        entity.setEquipment(slot, itemStack);
    }
}

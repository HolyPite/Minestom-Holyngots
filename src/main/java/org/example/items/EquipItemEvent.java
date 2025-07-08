package org.example.items;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.example.combats.AttackSpeedManager;

public class EquipItemEvent {
    public static void init(GlobalEventHandler events){
        events.addListener(EntityAttackEvent.class, e -> {
            if (e.getEntity() instanceof LivingEntity p){
                AttributeInstance att = p.getAttribute(Attribute.ATTACK_SPEED);
                //System.out.println(AttackSpeedManager.getCooldown(p));
                att.setBaseValue(20/AttackSpeedManager.getCooldown(p));
            };
        });
    }
}

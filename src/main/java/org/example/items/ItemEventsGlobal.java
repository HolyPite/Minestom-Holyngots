package org.example.items;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.timer.TaskSchedule;
import org.example.combats.AttackSpeedManager;
import org.example.items.datas.Stats;

public class ItemEventsGlobal {
    public static void init(EventNode<Event> events){

        //update attack speed
        events.addListener(EntityAttackEvent.class, e -> {
            if (e.getEntity() instanceof LivingEntity p){
                AttributeInstance att = p.getAttribute(Attribute.ATTACK_SPEED);
                //System.out.println(AttackSpeedManager.getCooldown(p));
                att.setBaseValue(20/AttackSpeedManager.getCooldown(p));
            };
        });

        events.addListener(InventoryItemChangeEvent.class, e -> {
            if (e.getInventory() instanceof PlayerInventory inv) {
                for (Player viewer : inv.getViewers()) {
                    Stats.refresh(viewer);
                }
            }
        });

        events.addListener(PlayerChangeHeldSlotEvent.class, e -> {
            Player p = e.getEntity();
            p.scheduler().buildTask(() -> {
                Stats.refresh(p);
            }).delay(TaskSchedule.tick(1)).schedule();
        });

    }
}

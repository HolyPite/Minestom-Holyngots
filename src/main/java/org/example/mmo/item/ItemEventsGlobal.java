package org.example.mmo.item;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.timer.TaskSchedule;
import org.example.InstancesInit;
import org.example.mmo.combat.AttackSpeedManager;
import org.example.mmo.item.datas.Stats;

import java.util.Objects;

public class ItemEventsGlobal {
    public static void init(EventNode<Event> events){

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();
        EventNode<EntityEvent> entityNode = events.findChildren("entityNode",EntityEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode",InventoryEvent.class).getFirst();

        //update attack speed
        entityNode.addListener(EntityAttackEvent.class, e -> {
            if (e.getEntity() instanceof LivingEntity p){
                AttributeInstance att = p.getAttribute(Attribute.ATTACK_SPEED);
                //System.out.println(AttackSpeedManager.getCooldown(p));
                att.setBaseValue(20/AttackSpeedManager.getCooldown(p));
            };
        });

        //update stats
        inventoryNode.addListener(InventoryItemChangeEvent.class, e -> {
            if (e.getInventory() instanceof PlayerInventory inv) {
                for (Player viewer : inv.getViewers()) {
                    if (Objects.equals(InstancesInit.instance_type_name_get(InstancesInit.instance_type_get(viewer.getInstance())), "games"))   {
                        Stats.refresh(viewer);
                    }
                }
            }
        });

        playerNode.addListener(PlayerChangeHeldSlotEvent.class, e -> {
            Player p = e.getEntity();
            p.scheduler().buildTask(() -> {
                if (Objects.equals(InstancesInit.instance_type_name_get(InstancesInit.instance_type_get(p.getInstance())), "games"))   {
                    Stats.refresh(p);
                }
            }).delay(TaskSchedule.tick(1)).schedule();
        });

    }
}

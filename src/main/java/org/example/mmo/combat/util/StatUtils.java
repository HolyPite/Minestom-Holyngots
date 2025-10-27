package org.example.mmo.combat.util;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemUtils;
import org.example.mmo.item.datas.StatType;

import static org.example.mmo.item.datas.StatMap.ATTR_MAP;

/**
 * Utility class for calculating cumulative stats from entities and items.
 */
public final class StatUtils {

    private StatUtils() {}

    /* -------- Accès aux stats cumulées -------- */
    public static int getTotal(LivingEntity ent, StatType type) {
        int sum = 0;
        sum += fromAttribute(ent, type);
        sum += fromStack(ent.getItemInMainHand(), type);
        sum += fromStack(ent.getItemInOffHand(),  type);
        for (EquipmentSlot slot : EquipmentSlot.armors())
            sum += fromStack(ent.getEquipment(slot), type);

        if (type.kind == StatType.ValueKind.PERCENT && sum < -99) sum =-99;
        if (type.kind == StatType.ValueKind.PROBA && sum < 0) sum = 0;

        return sum;
    }



    private static int fromStack(ItemStack stack, StatType type) {
        GameItem gi = ItemUtils.resolve(stack);
        return gi == null ? 0 : gi.stats.getOrDefault(type, 0);
    }
    private static int fromAttribute(LivingEntity ent, StatType type) {
        var attr = ATTR_MAP.get(type);
        var inst = attr == null ? null : ent.getAttribute(attr);
        return inst == null ? 0 : (int) Math.round(inst.getValue());
    }
}

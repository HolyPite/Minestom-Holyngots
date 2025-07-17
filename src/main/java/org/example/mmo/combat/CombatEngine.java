// org.example.mmo.combats.CombatEngine
package org.example.mmo.combat;

import net.minestom.server.entity.*;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.*;
import org.example.mmo.item.datas.StatType;

import java.util.concurrent.ThreadLocalRandom;

import static org.example.mmo.item.datas.StatMap.ATTR_MAP;

public final class CombatEngine {

    public static double computeDamage(LivingEntity attacker, LivingEntity victim) {

        /* ---------- Stats cumulées ---------- */
        double atk        = getTotal(attacker, StatType.ATTACK);
        double armor      = getTotal(victim  , StatType.ARMOR);
        double pen        = getTotal(attacker, StatType.ARMOR_PEN)   / 100.0;
        double critChance = getTotal(attacker, StatType.CRIT_CHANCE) / 100.0;
        double critValue  = 1 + getTotal(attacker, StatType.CRIT_VALUE) / 100.0;
        double dodge      = getTotal(victim  , StatType.DODGE)       / 100.0;
        double kbBonus    = getTotal(attacker, StatType.KNOCKBACK)   / 100.0;
        double kbRes      = getTotal(victim  , StatType.KNOCKBACK_RES)/ 100.0;
        double lifesteal  = getTotal(attacker, StatType.LIFESTEAL)   / 100.0;
        double stunChance = getTotal(attacker, StatType.STUN_CHANCE) / 100.0;

        /* ---------- Cool-down / “charge” ---------- */
        double charge   = AttackSpeedManager.getCharge(attacker);
        double cdFactor = computeCDFactor(charge);
        //System.out.println(cdFactor);
        AttackSpeedManager.markAttack(attacker);

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        /* ---------- 1. Esquive ---------- */
        if (rnd.nextDouble() < dodge) {
            CombatFeedback.showDodge(victim);
            return 0;
        }

        /* ---------- 2. Dégâts de base ---------- */
        double effArmor = Math.max(0, armor * Math.max(0, 1 - pen));
        double dmg      = Math.max(0, atk - effArmor / 2.0) * cdFactor;

        /* ---------- 3. Critique ---------- */
        if (rnd.nextDouble() < critChance * cdFactor) {
            dmg *= critValue;
            CombatFeedback.showCrit(attacker, victim);
        }

        /* ---------- 4. Knock-back ---------- */
        double kbFactor = (1 + kbBonus) * (1 - kbRes) * cdFactor;
        if (kbFactor > 0) KnockbackUtils.apply(attacker, victim, kbFactor);

        /* ---------- 5. Vol de vie ---------- */
        if (lifesteal > 0) {
            double heal = dmg * lifesteal * cdFactor;
            HealthUtils.heal(attacker, heal);
            CombatFeedback.showHeal(attacker, heal);
        }

        /* ---------- 6. Étourdissement ---------- */
        if (stunChance > 0 && rnd.nextDouble() < stunChance * cdFactor) {
            // StunManager.applyStun(victim, 20);
        }

        /* ---------- 7. Application des dégâts ---------- */
        HealthUtils.damage(victim, dmg);
        CombatFeedback.showHit(victim);
        if(victim instanceof Player p) return dmg;
        HealthUtils.updateHealthBar(victim);
        return dmg;
    }



    /* -------- Charge-Damage factor -------- */
    private static double computeCDFactor(double charge) {
        if (charge < 0.20)        return 0;                     // spam total
        if (charge < 0.99)        return 0.70 * charge * charge;
        if (charge <= 1.01)       return 1.05;                  // perfect timing
        return 1.0;                                             // fully recharged
    }



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



    private CombatEngine() {}
}

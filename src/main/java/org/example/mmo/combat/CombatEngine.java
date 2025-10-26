package org.example.mmo.combat;

import net.minestom.server.entity.LivingEntity;
import org.example.mmo.combat.mechanic.AttackSpeedManager;
import org.example.mmo.combat.util.CombatFeedback;
import org.example.mmo.combat.util.KnockbackUtils;
import org.example.mmo.combat.util.StatUtils;
import org.example.mmo.item.datas.StatType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A pure calculation engine for combat.
 * Its only responsibility is to calculate the final damage amount based on stats.
 */
public final class CombatEngine {

    public static double computeDamage(LivingEntity attacker, LivingEntity victim) {

        /* ---------- Stats ---------- */
        double atk        = StatUtils.getTotal(attacker, StatType.ATTACK);
        double armor      = StatUtils.getTotal(victim  , StatType.ARMOR);
        double pen        = StatUtils.getTotal(attacker, StatType.ARMOR_PEN)   / 100.0;
        double critChance = StatUtils.getTotal(attacker, StatType.CRIT_CHANCE) / 100.0;
        double critValue  = 1 + StatUtils.getTotal(attacker, StatType.CRIT_VALUE) / 100.0;
        double dodge      = StatUtils.getTotal(victim  , StatType.DODGE)       / 100.0;
        double kbBonus    = StatUtils.getTotal(attacker, StatType.KNOCKBACK)   / 100.0;
        double kbRes      = StatUtils.getTotal(victim  , StatType.KNOCKBACK_RES)/ 100.0;

        /* ---------- Attack Charge ---------- */
        double charge   = AttackSpeedManager.getCharge(attacker);
        double cdFactor = computeCDFactor(charge);
        AttackSpeedManager.markAttack(attacker);

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        /* ---------- 1. Dodge ---------- */
        if (rnd.nextDouble() < dodge) {
            CombatFeedback.showDodge(victim);
            return 0; // No damage if dodged
        }

        /* ---------- 2. Base Damage ---------- */
        double effArmor = Math.max(0, armor * Math.max(0, 1 - pen));
        double dmg      = Math.max(0, atk - effArmor / 2.0) * cdFactor;

        /* ---------- 3. Critical Hit ---------- */
        if (rnd.nextDouble() < critChance * cdFactor) {
            dmg *= critValue;
            CombatFeedback.showCrit(attacker, victim);
        }

        /* ---------- 4. Knockback ---------- */
        double kbFactor = (1 + kbBonus) * (1 - kbRes) * cdFactor;
        if (kbFactor > 0) KnockbackUtils.apply(attacker, victim, kbFactor);

        // Lifesteal and Stun are now handled in the listener after the final damage is confirmed.

        return dmg;
    }

    private static double computeCDFactor(double charge) {
        if (charge < 0.20)        return 0;
        if (charge < 0.99)        return 0.70 * charge * charge;
        if (charge <= 1.01)       return 1.05;
        return 1.0;
    }

    private CombatEngine() {}
}

package org.example.mmo.combat;

import net.minestom.server.entity.LivingEntity;
import org.example.mmo.item.datas.StatType;

// org.example.mmo.combats.AttackSpeedManager
public final class AttackSpeedManager {

    public static double getCharge(LivingEntity attacker) {

        long now = attacker.getInstance().getTime();      // tick courant
        Long lastObj = attacker.getTag(CombatTags.LAST_HIT_TICK);

        if (lastObj == null) return 1.0;

        double charge = (now - lastObj) / getCooldown(attacker);
        //System.out.println(charge);
        return Math.min(charge, 2.0);    // clamp éventuel (optionnel)
    }

    public static double getCooldown(LivingEntity attacker) {
        double atkSpeedPct = CombatEngine.getTotal(attacker, StatType.ATTACK_SPEED);
        double atkSpeedBase = 10.0; //tick
        double cooldown    = atkSpeedBase / (1 + (atkSpeedPct / 100.0));   // ticks
        //System.out.println(atkSpeedPct);
        return Math.min(200,Math.max(1.0, cooldown));                      // sécurité div/0 et <10 sec
    }

    /** Mémorise la date du coup (à appeler dès que le swing est validé) */
    public static void markAttack(LivingEntity attacker) {
        attacker.setTag(CombatTags.LAST_HIT_TICK, attacker.getInstance().getTime());
    }

    private AttackSpeedManager() {}
}


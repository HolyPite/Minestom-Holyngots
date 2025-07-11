package org.example.combats;

import net.minestom.server.entity.LivingEntity;
import org.example.items.StatType;

// org.example.combats.AttackSpeedManager
public final class AttackSpeedManager {

    public static double getCharge(LivingEntity attacker) {

        long now = attacker.getInstance().getTime();      // tick courant
        Long lastObj = attacker.getTag(CombatTags.LAST_ATTACK_TICK);

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

    public static boolean getImmunity(LivingEntity victim) {

        // Tick courant de l’instance
        long now = victim.getInstance().getTime();

        // Dernier tick où la cible a pris un coup
        Long last = victim.getTag(CombatTags.LAST_VICTIM_TICK);
        if (last == null) return false;               // jamais touché → pas d’immunité

    /* 0,2 s  ≃ 4 ticks (20 t/s) :
       True  → encore sous « immunité »
       False → on peut de nouveau infliger des dégâts           */
        return now - last < 10;
    }


    /** Mémorise la date du coup (à appeler dès que le swing est validé) */
    public static void markAttack(LivingEntity attacker, LivingEntity victim) {
        attacker.setTag(CombatTags.LAST_ATTACK_TICK, attacker.getInstance().getTime());
        victim.setTag(CombatTags.LAST_VICTIM_TICK, victim.getInstance().getTime());
    }

    private AttackSpeedManager() {}
}


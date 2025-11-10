package org.example.mmo.combat.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;

public class KnockbackUtils {
    public static void apply(LivingEntity attacker, LivingEntity victim, double power) {
        Pos attackerPos = attacker.getPosition();
        Pos victimPos = victim.getPosition();
        Vec knockbackDir = victimPos.sub(attackerPos).asVec().normalize();
        victim.setVelocity(knockbackDir.mul(power * 40));
    }
}

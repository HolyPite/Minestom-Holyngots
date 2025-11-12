package org.example.mmo.combat.mechanic;

import net.minestom.server.entity.LivingEntity;
import org.example.mmo.combat.util.StatUtils;
import org.example.mmo.item.datas.StatType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttackSpeedManager {
    private static final Map<UUID, Long> LAST_ATTACK_TIMES = new ConcurrentHashMap<>();

    public static void markAttack(LivingEntity attacker) {
        LAST_ATTACK_TIMES.put(attacker.getUuid(), System.currentTimeMillis());
    }

    /**
     * Gets the attack cooldown in milliseconds for a given entity.
     * @param attacker The entity.
     * @return The cooldown time in milliseconds.
     */
    public static double getAttackCooldown(LivingEntity attacker) {
        double speed = StatUtils.getTotal(attacker, StatType.ATTACK_SPEED) / 100.0;
        return 1000.0 / (1 + speed);
    }

    public static double getCharge(LivingEntity attacker) {
        Long lastAttack = LAST_ATTACK_TIMES.get(attacker.getUuid());
        if (lastAttack == null) return 1.0; // full charge on first attack

        double cd = getAttackCooldown(attacker);
        long timeSince = System.currentTimeMillis() - lastAttack;
        return Math.min(1.5, timeSince / cd);
    }
}

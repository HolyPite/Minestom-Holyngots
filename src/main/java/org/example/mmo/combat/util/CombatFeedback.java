package org.example.mmo.combat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;

public final class CombatFeedback {

    private CombatFeedback() {
    }

    private static void sendActionBar(LivingEntity entity, Component message) {
        if (entity instanceof Player player) {
            player.sendActionBar(message);
        }
    }

    public static void showDodge(LivingEntity victim) {
        sendActionBar(victim, Component.text("Esquive !", NamedTextColor.AQUA));
    }

    public static void showCrit(LivingEntity attacker, LivingEntity victim) {
        Component crit = Component.text("Coup critique !", NamedTextColor.GOLD);
        sendActionBar(attacker, crit);
        sendActionBar(victim, crit);
    }

    public static void showHeal(LivingEntity target, double amount) {
        int rounded = (int) Math.round(amount);
        sendActionBar(target, Component.text("+" + rounded + " PV", NamedTextColor.GREEN));
    }

    public static void showHit(LivingEntity victim, float amount) {
        int rounded = Math.max(1, Math.round(amount));
        sendActionBar(victim, Component.text("−" + rounded + " PV", NamedTextColor.RED));
    }
}

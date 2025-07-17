package org.example.mmo.combat;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.particle.Particle;
import org.example.utils.TKit;

/**
 * Petit utilitaire statique : effets visuels/sonores liés au système de combat.
 *
 *  • Aucune logique de dégâts / stats ici – uniquement du « feedback ».
 *  • On garde des méthodes très simples : on leur passe l’entité concernée,
 *    la méthode s’occupe de jouer un son + particules à sa position.
 */
public final class CombatFeedback {

    /* ======================  API  ====================== */

    public static void showHit(LivingEntity victim) {
        if (victim.isRemoved()) return;

        // Son de coup : bruit de frappe (variant « strong attack »)
        TKit.playSound(
                victim.getInstance(), victim.getPosition(),
                "entity.player.attack.strong", Sound.Source.PLAYER,
                1.3f, 1.0f
        );

        // Particules de dégâts (rouges)
        TKit.spawnParticles(
                victim.getInstance(), Particle.DAMAGE_INDICATOR,
                victim.getPosition().add(0, 1, 0),
                0.3f, 0.3f, 0.3f,
                0f, 10
        );
    }

    public static void showDodge(LivingEntity victim) {
        if (victim.isRemoved()) return;

        // Son d’esquive : un petit « swoosh »
        TKit.playSound(victim.getInstance(), victim.getPosition(),
                "entity.player.attack.sweep", Sound.Source.PLAYER,
                1.5f, 1.4f);

        // Particules de fumée légère autour de la tête
        TKit.spawnParticles(victim.getInstance(), Particle.CLOUD,
                victim.getPosition().add(0, 1, 0),
                0.3f, 0.3f, 0.3f,
                0f, 20);
    }

    public static void showCrit(LivingEntity attacker, LivingEntity victim) {
        if (victim.isRemoved()) return;

        // Son critique : le « crit » de Minecraft
        TKit.playSound(victim.getInstance(), victim.getPosition(),
                "entity.player.attack.crit", Sound.Source.PLAYER,
                1.8f, 1.0f);

        // Particules critique (type CRIT, violet)
        TKit.spawnParticles(victim.getInstance(), Particle.CRIT,
                victim.getPosition().add(0, 1, 0),
                0.4f, 0.4f, 0.4f,
                0f, 30);
    }

    public static void showHeal(LivingEntity entity, double amount) {
        // Son léger + particules « happy villager »
        TKit.playSound(entity.getInstance(), entity.getPosition(),
                "entity.player.levelup", Sound.Source.PLAYER,
                0.8f, 1.6f);

        TKit.spawnParticles(entity.getInstance(), Particle.HEART,
                entity.getPosition().add(0, 1.2, 0),
                0.2f, 0.4f, 0.2f,
                0f, 8);
    }

    private CombatFeedback() {}
}

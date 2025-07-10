package org.example.combats;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.example.utils.TKit;

/** Aucune logique de dégâts ici – juste le déplacement. */
public final class KnockbackUtils {

    public static void apply(LivingEntity attacker,
                             LivingEntity victim,
                             double factor) {

        if (factor <= 0 || victim.isRemoved()) return;

        /* direction horizontale (XZ) : victime ← attacker */
        Vec dir;
        if (attacker == null) {
            dir = victim.getPosition().direction().mul(-1);   // « pousser vers l’arrière »
        } else {
            dir = TKit.getDirection(attacker.getPosition(), victim.getPosition());
        }

        dir = dir.normalize();
        if (dir.lengthSquared() < 1e-4) dir = new Vec(0, 0, 0);   // sécurité

        /* constantes de base (à ajuster) */
        final double HORIZONTAL = 6.0;   // vitesse latérale
        final double VERTICAL   = 4.0;   // vitesse montée

        Vec kb = dir.mul(HORIZONTAL * factor)
                .withY(VERTICAL * factor);

        victim.setVelocity(kb);
    }

    private KnockbackUtils() {}
}

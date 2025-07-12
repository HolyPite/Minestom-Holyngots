// org.example.mmo.combats.HealthUtils
package org.example.mmo.combats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.LivingEntity;
import org.example.mmo.items.datas.StatType;

public final class HealthUtils {

    /* -- lecture / conversion -- */

    public static void updateHealthBar(LivingEntity entity) {
        final int totalBars = 20;
        float max = (float) getCustomMax(entity);
        float hp = (float) getCustom(entity);

        int greenBars = Math.round((hp / max) * totalBars);
        int redBars = totalBars - greenBars;

        // Choisis le caractère de barre (ici █ pour bien remplir, tu peux mettre |, ▌, etc)
        String barSymbol = "█";

        TextComponent.Builder healthBar = Component.text();

        // Vert pour la vie restante
        for (int i = 0; i < greenBars; i++) {
            healthBar.append(Component.text(barSymbol).color(NamedTextColor.GREEN));
        }

        // Rouge pour la vie perdue
        for (int i = 0; i < redBars; i++) {
            healthBar.append(Component.text(barSymbol).color(NamedTextColor.DARK_RED));
        }

        // Affiche la vie au centre (hp arrondi)
        int displayHP = Math.round(hp);
        int displayMax = Math.round(max);

        // Place la vie au centre, remplace le milieu par le texte (optionnel)
        int center = totalBars / 2;
        TextComponent mid = Component.text(displayHP + "/" + displayMax, NamedTextColor.YELLOW);

        // Reconstruis la barre en insérant le texte au centre
        TextComponent.Builder finalBar = Component.text();
        int i = 0;
        for (Component c : healthBar.build().children()) {
            if (i == center) {
                finalBar.append(mid);
            }
            finalBar.append(c);
            i++;
        }

        // Mets à jour le nom
        entity.set(DataComponents. CUSTOM_NAME, finalBar.build());
        entity.setCustomNameVisible(true);
    }


    /** PV maxi personnalisés = 20 + bonus HEALTH de l’équipement. */
    public static double getCustomMax(LivingEntity e) {
        return CombatEngine.getTotal(e, StatType.HEALTH);
    }

    /** PV courants (échelle personnalisée). */
    public static double getCustom(LivingEntity e) {
        return e.getHealth() * getCustomMax(e) / 20.0;
    }

    /** Applique une valeur personnalisée -> met à jour le health vanilla. */
    private static void setCustom(LivingEntity e, double value) {
        double clamped = Math.max(0, Math.min(value, getCustomMax(e)));
        e.setHealth((float) (clamped * 20.0 / getCustomMax(e)));
    }

    /* -- API publique -- */

    public static void heal(LivingEntity e, double amount) {
        setCustom(e, getCustom(e) + amount);
    }

    public static void damage(LivingEntity e, double amount) {
        setCustom(e, getCustom(e) - amount);
    }

    private HealthUtils() {}
}

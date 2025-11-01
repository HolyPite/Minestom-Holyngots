package org.example.mmo.combat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

import java.util.Locale;

/**
 * Centralised combat feedback helpers (action bar messaging).
 */
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

    public static void showHit(LivingEntity attacker,
                               LivingEntity victim,
                               float amount,
                               float victimHealth,
                               float victimMaxHealth,
                               RegistryKey<DamageType> damageType) {
        int rounded = Math.max(1, Math.round(amount));
        Component damageNumber = Component.text("-" + rounded + " PV", NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);

        Component typeLabel = resolveTypeLabel(damageType);
        Component healthInfo = Component.empty();
        if (victimMaxHealth > 0f) {
            int percent = Math.round(Math.max(0f, Math.min(1f, victimHealth / victimMaxHealth)) * 100f);
            healthInfo = Component.text(" " + Math.round(victimHealth) + "/" + Math.round(victimMaxHealth) + " (" + percent + "%)", NamedTextColor.GRAY);
        }

        Component victimMessage = Component.text()
                .append(damageNumber)
                .append(Component.text(" "))
                .append(typeLabel)
                .append(healthInfo)
                .build();
        sendActionBar(victim, victimMessage);

        if (attacker instanceof Player player) {
            TextComponent.Builder builder = Component.text();
            builder.append(displayName(victim));
            if (victimMaxHealth > 0f) {
                builder.append(Component.text(" - ", NamedTextColor.DARK_GRAY));
                builder.append(Component.text(Math.round(victimHealth) + " PV", chooseColor(victimHealth, victimMaxHealth)));
                builder.append(Component.text(" (-" + rounded + ")", NamedTextColor.DARK_RED));
            }
            player.sendActionBar(builder.build());
        }
    }

    private static Component resolveTypeLabel(RegistryKey<DamageType> damageType) {
        String path = "generic";
        if (damageType != null) {
            path = damageType.name();
            int colon = path.indexOf(':');
            if (colon >= 0 && colon < path.length() - 1) {
                path = path.substring(colon + 1);
            }
        }
        String label = path.replace('_', ' ');
        TextColor color = switch (path) {
            case "fire", "lava", "on_fire" -> NamedTextColor.GOLD;
            case "magic", "magic_player", "indirect_magic" -> NamedTextColor.LIGHT_PURPLE;
            case "projectile" -> NamedTextColor.BLUE;
            case "explosion", "player_explosion" -> NamedTextColor.DARK_RED;
            case "poison", "wither" -> NamedTextColor.DARK_GREEN;
            default -> NamedTextColor.RED;
        };
        return Component.text("[" + capitalize(label) + "]", color).decorate(TextDecoration.BOLD);
    }

    private static Component displayName(LivingEntity entity) {
        Component custom = entity.getCustomName();
        if (custom != null) {
            return custom;
        }
        String typeName = entity.getEntityType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Component.text(capitalize(typeName), NamedTextColor.WHITE);
    }

    private static TextColor chooseColor(float health, float maxHealth) {
        if (maxHealth <= 0f) {
            return NamedTextColor.GREEN;
        }
        float ratio = health / maxHealth;
        if (ratio > 0.6f) {
            return NamedTextColor.GREEN;
        }
        if (ratio > 0.3f) {
            return NamedTextColor.GOLD;
        }
        return NamedTextColor.RED;
    }

    private static String capitalize(String input) {
        if (input.isEmpty()) {
            return input;
        }
        char first = Character.toTitleCase(input.charAt(0));
        if (input.length() == 1) {
            return String.valueOf(first);
        }
        return first + input.substring(1);
    }
}

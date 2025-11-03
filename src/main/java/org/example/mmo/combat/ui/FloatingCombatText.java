package org.example.mmo.combat.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns short-lived floating text indicators for combat events.
 */
public final class FloatingCombatText {

    private static final int LIFETIME_TICKS = 25;

    private FloatingCombatText() {
    }

    public static void showDamage(@NotNull LivingEntity target,
                                  float amount,
                                  RegistryKey<DamageType> damageType) {
        int rounded = Math.max(1, Math.round(amount));
        NamedTextColor color = chooseColor(damageType);
        Component text = Component.text("-" + rounded, color);
        spawn(target, text);
    }

    public static void showHeal(@NotNull LivingEntity target, double amount) {
        int rounded = Math.max(1, (int) Math.round(amount));
        Component text = Component.text("+" + rounded, NamedTextColor.GREEN);
        spawn(target, text);
    }

    private static void spawn(LivingEntity target, Component text) {
        if (target.getInstance() == null) {
            return;
        }

        Entity indicator = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) indicator.getEntityMeta();
        meta.setText(text);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setShadow(true);
        meta.setBackgroundColor(0x00000000);
        meta.setLineWidth(0);

        indicator.setNoGravity(true);
        indicator.setInstance(target.getInstance(), randomOffset(target));


        var scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(() -> {
                    if (!indicator.isRemoved()) {
                        indicator.remove();
                    }
                })
                .delay(TaskSchedule.tick(LIFETIME_TICKS))
                .schedule();

        scheduler.buildTask(() -> {
                    if (indicator.isRemoved()) {
                        return;
                    }
                    Pos current = indicator.getPosition();
                    indicator.teleport(current.withY(current.y() + 0.04));
                })
                .delay(TaskSchedule.tick(2))
                .repeat(TaskSchedule.tick(2))
                .schedule();
    }

    private static NamedTextColor chooseColor(RegistryKey<DamageType> damageType) {
        String path = "generic";
        if (damageType != null) {
            path = damageType.name();
            int colon = path.indexOf(':');
            if (colon >= 0 && colon < path.length() - 1) {
                path = path.substring(colon + 1);
            }
        }
        return switch (path) {
            case "fire", "lava", "on_fire" -> NamedTextColor.GOLD;
            case "magic", "magic_player", "indirect_magic" -> NamedTextColor.LIGHT_PURPLE;
            case "projectile" -> NamedTextColor.BLUE;
            case "explosion", "player_explosion" -> NamedTextColor.DARK_RED;
            case "poison", "wither" -> NamedTextColor.DARK_GREEN;
            default -> NamedTextColor.RED;
        };
    }

    private static Pos randomOffset(LivingEntity target) {
        double eyeHeight = target.getEyeHeight();
        Pos base = target.getPosition();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double x = base.x() + (rnd.nextDouble() - 0.5) * 0.6;
        double y = base.y() + eyeHeight + 0.3 + rnd.nextDouble() * 0.2;
        double z = base.z() + (rnd.nextDouble() - 0.5) * 0.6;
        return new Pos(x, y, z, base.yaw(), base.pitch());
    }
}

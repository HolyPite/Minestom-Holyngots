package org.example.mmo.player.respawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

/**
 * Centralizes the static respawn stone declarations.
 * Update this class when adding or moving stones in the world.
 */
public final class RespawnStoneDefinitions {

    private RespawnStoneDefinitions() {
    }

    public static void registerDefaults(RespawnStoneRegistry registry) {
        registry.register(new RespawnStoneDefinition(
                "camp_0_40_0",
                0,
                40,
                0,
                new Pos(0.5, 40, 0.5),
                Component.text("Pierre du Camp (0,40,0)", NamedTextColor.GOLD)
        ));

        registry.register(new RespawnStoneDefinition(
                "camp_20_50_0",
                20,
                40,
                0,
                new Pos(20.5, 40, 0.5),
                Component.text("Pierre du Plateau (20,40,0)", NamedTextColor.AQUA)
        ));

        registry.register(new RespawnStoneDefinition(
                "camp_0_50_20",
                0,
                40,
                20,
                new Pos(0.5, 40, 20.5),
                Component.text("Pierre des Falaises (0,40,20)", NamedTextColor.LIGHT_PURPLE)
        ));
    }
}

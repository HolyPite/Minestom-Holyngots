package org.example.mmo.npc.npcs;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;

import java.util.List;

public final class GuideNpc {
    public static final NPC INSTANCE;

    static {
        INSTANCE = new NPC(
                "guide",
                Component.text("Guide Ancien", NamedTextColor.DARK_PURPLE),
                new Pos(0, 40, 0, 180, 0), // Facing towards the hunting ground
                List.of(
                        Component.text("Les échos du passé résonnent en ces lieux..."),
                        Component.text("Cherchez la force qui est en vous."),
                        Component.text("Le chemin est long, mais la récompense est grande."),
                        Component.text("Ne vous fiez pas aux apparences."),
                        Component.text("Les ténèbres ne sont jamais loin.")
                ),
                Sound.sound(Key.key("minecraft:entity.warden.ambient"), Sound.Source.NEUTRAL, 1.5f, 0.8f)
        );
        NpcRegistry.register(INSTANCE);
    }

    private GuideNpc() {}
}

package org.example.mmo.npc.npcs;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;

import java.util.List;

public final class GuideNpc {
    public static final NPC INSTANCE;

    static {
        INSTANCE = new NPC(
                "guide",
                Component.text("Guide Ancien", NamedTextColor.DARK_PURPLE),
                EntityType.WARDEN,
                new Pos(0, 40, 0, 180, 0),
                List.of(
                        Component.text("Les échos du passé résonnent en ces lieux..."),
                        Component.text("Cherchez la force qui est en vous.")
                ),
                Sound.sound(Key.key("minecraft:entity.warden.ambient"), Sound.Source.NEUTRAL, 1.5f, 0.8f)
        );
        NpcRegistry.register(INSTANCE);
    }

    private GuideNpc() {}
}

package org.example.mmo.npc.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;

import java.util.List;

public final class HunterNpc {
    public static final NPC INSTANCE;

    static {
        INSTANCE = new NPC(
                "hunter",
                Component.text("Chasseur", NamedTextColor.GOLD),
                new Pos(-2, 40, 0, 90, 0),
                List.of(
                        Component.text("La nature est impitoyable. Soyez-le aussi."),
                        Component.text("J'ai vu des traces étranges près du bois...")
                )
        );
        NpcRegistry.register(INSTANCE);
    }

    private HunterNpc() {}
}

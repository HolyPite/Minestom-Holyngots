package org.example.mmo.npc.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;

import java.util.List;

public final class PriestNpc {
    public static final NPC INSTANCE;

    static {
        INSTANCE = new NPC(
                "priest",
                Component.text("Prêtre", NamedTextColor.AQUA),
                new Pos(2, 40, 0, -90, 0),
                List.of(
                        Component.text("Que la lumière guide vos pas."),
                        Component.text("Attention aux ombres qui rôdent.")
                )
        );
        NpcRegistry.register(INSTANCE);
    }

    private PriestNpc() {}
}

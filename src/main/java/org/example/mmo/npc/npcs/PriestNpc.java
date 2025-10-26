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
                new Pos(2, 40, 0, -90, 0), // Facing the hunter
                List.of(
                        Component.text("Que la lumière guide vos pas."),
                        Component.text("Attention aux ombres qui rôdent."),
                        Component.text("Une prière peut parfois repousser le mal."),
                        Component.text("N'oubliez jamais l'espoir.")
                )
        );
        NpcRegistry.register(INSTANCE);
    }

    private PriestNpc() {}
}

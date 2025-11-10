package org.example.mmo.npc.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;

import java.util.List;

public final class WizardNpc {
    public static final NPC INSTANCE;

    static {
        INSTANCE = new NPC(
                "wizard",
                Component.text("Archimage", NamedTextColor.LIGHT_PURPLE),
                new Pos(5, 40, 5),
                List.of(
                        Component.text("L'énergie arcanique est instable aujourd'hui..."),
                        Component.text("Ne touchez pas à cette fiole !")
                )
        );
        NpcRegistry.register(INSTANCE);
    }

    private WizardNpc() {}
}

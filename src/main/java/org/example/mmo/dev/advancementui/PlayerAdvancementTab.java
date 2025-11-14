package org.example.mmo.dev.advancementui;

import net.minestom.server.advancements.Advancement;
import net.minestom.server.advancements.AdvancementTab;

import java.util.Map;

/**
 * Contient l'onglet et les nœuds correspondants à un joueur et un arbre donné.
 */
public record PlayerAdvancementTab(
        String identifier,
        AdvancementTab tab,
        Map<String, Advancement> nodes
) {

    public Advancement node(String nodeId) {
        return nodes.get(nodeId);
    }
}

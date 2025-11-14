package org.example.mmo.dev.advancementui;

import net.minestom.server.entity.Player;
import org.example.mmo.dev.advancementui.model.SkillTreePrototype;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère la relation joueur -> onglets d'advancement personnalisés.
 */
public final class PlayerAdvancementTabController {

    private final AdvancementTabFactory tabFactory = new AdvancementTabFactory();
    private final Map<UUID, Map<String, PlayerAdvancementTab>> tabsByPlayer = new ConcurrentHashMap<>();

    public PlayerAdvancementTab openTree(Player player, SkillTreePrototype prototype) {
        Map<String, PlayerAdvancementTab> tabs = tabsByPlayer.computeIfAbsent(player.getUuid(),
                uuid -> new ConcurrentHashMap<>());
        PlayerAdvancementTab playerTab = tabs.computeIfAbsent(prototype.id(),
                id -> tabFactory.create(player.getUuid(), prototype));
        playerTab.tab().addViewer(player);
        return playerTab;
    }

    public void closeAll(Player player) {
        Map<String, PlayerAdvancementTab> tabs = tabsByPlayer.get(player.getUuid());
        if (tabs == null) {
            return;
        }
        tabs.values().forEach(tab -> tab.tab().removeViewer(player));
    }

    public PlayerAdvancementTab get(Player player, String treeId) {
        Map<String, PlayerAdvancementTab> tabs = tabsByPlayer.get(player.getUuid());
        if (tabs == null) {
            return null;
        }
        return tabs.get(treeId);
    }
}

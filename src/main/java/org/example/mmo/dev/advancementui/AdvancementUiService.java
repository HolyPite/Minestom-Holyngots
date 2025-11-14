package org.example.mmo.dev.advancementui;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.dev.advancementui.model.SkillTreePrototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service principal pour l'UI Advancement-based (affichage uniquement).
 */
public final class AdvancementUiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancementUiService.class);

    private static AdvancementUiService instance;

    private final AdvancementUiRegistry registry;
    private final PlayerAdvancementTabController tabController;
    private final InstanceRegistry instances;

    private AdvancementUiService(GlobalEventHandler events, InstanceRegistry instances) {
        this.registry = new AdvancementUiRegistry(new SkillTreeDefinitionLoader(), new AdvancementGraphBuilder());
        this.registry.reload();
        this.tabController = new PlayerAdvancementTabController();
        this.instances = instances;
        registerListeners(events);
    }

    public static AdvancementUiService init(GlobalEventHandler events, InstanceRegistry instances) {
        if (instance == null) {
            instance = new AdvancementUiService(events, instances);
        }
        return instance;
    }

    public static AdvancementUiService get() {
        return instance;
    }

    private void registerListeners(GlobalEventHandler events) {
        events.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }
            if (!instances.isGameInstance(event.getSpawnInstance())) {
                return;
            }
            boolean opened = openTree(event.getPlayer(), "basic_combat"); // arbre démo
            if (!opened) {
                LOGGER.warn("Failed to auto-open basic_combat skill tree for {}", event.getPlayer().getUsername());
            }
        });
        events.addListener(PlayerDisconnectEvent.class, event -> tabController.closeAll(event.getPlayer()));
    }

    public boolean openTree(Player player, String treeId) {
        SkillTreePrototype prototype = registry.get(treeId);
        if (prototype == null) {
            LOGGER.warn("Requested skill tree '{}' but no prototype is registered", treeId);
            return false;
        }
        tabController.openTree(player, prototype);
        LOGGER.debug("Opened advancement tree '{}' for {}", treeId, player.getUsername());
        return true;
    }

    public void closeAll(Player player) {
        tabController.closeAll(player);
    }

    public void reload() {
        registry.reload();
    }

    public int treeCount() {
        return registry.size();
    }

    public java.util.Collection<String> treeIds() {
        return registry.ids();
    }

    public boolean revealNode(Player player, String treeId, String nodeId) {
        return tabController.revealSecretNode(player, treeId, nodeId);
    }
}

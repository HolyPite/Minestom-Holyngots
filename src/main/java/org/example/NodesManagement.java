package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import org.example.commands.CommandRegister;
import org.example.mmo.combats.CombatListener;
import org.example.data.JsonPlayerDataRepository;
import org.example.data.PlayerDataService;
import org.example.mmo.items.ItemBootstrap;
import org.example.mmo.items.ItemEventsCustom;
import org.example.mmo.items.ItemEventsGlobal;

import java.util.Objects;

/**
 * Déclare et relie tous les {@link EventNode} nécessaires au gameplay,
 * puis initialise les différents systèmes (combat, items, persistance…).
 */
public final class NodesManagement {

    /* ------------------------------------------------------------------ */
    /* Définition / hiérarchie des EventNodes                              */
    /* ------------------------------------------------------------------ */

    private static final GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();

    /** Nœud racine pour tout ce qui est « in-game ». */
    private static final EventNode<Event> GAME_NODE = EventNode.all("gameNode");

    private static final EventNode<PlayerEvent> PLAYER_NODE =
            EventNode.event("playerNode", EventFilter.PLAYER,
                    event -> {
                        Instance inst = event.getPlayer().getInstance();
                        return inst != null && InstancesInit.GAME_INSTANCES.contains(inst);
                    });

    private static final EventNode<EntityEvent> ENTITY_NODE =
            EventNode.event("entityNode", EventFilter.ENTITY,
                    event -> {
                        Instance inst = event.getEntity().getInstance();
                        return inst != null && InstancesInit.GAME_INSTANCES.contains(inst);
                    });

    private static final EventNode<InventoryEvent> INVENTORY_NODE =
            EventNode.event("inventoryNode", EventFilter.INVENTORY,
                    event -> event.getInventory().getViewers().stream()
                            .map(Entity::getInstance)
                            .filter(Objects::nonNull)
                            .anyMatch(InstancesInit.GAME_INSTANCES::contains));

    /* ------------------------------------------------------------------ */
    /* Services et systèmes                                                */
    /* ------------------------------------------------------------------ */

    private static final PlayerDataService DATA_SERVICE =
            new PlayerDataService(new JsonPlayerDataRepository());

    /* ------------------------------------------------------------------ */
    /* Bloc static : exécuté une seule fois au chargement de la classe     */
    /* ------------------------------------------------------------------ */
    static {
        // S’assurer que les instances existent déjà
        // InstancesInit.init();

        // — Hiérarchie des nœuds
        GAME_NODE.addChild(PLAYER_NODE);
        GAME_NODE.addChild(ENTITY_NODE);
        GAME_NODE.addChild(INVENTORY_NODE);

        GLOBAL_EVENTS.addChild(GAME_NODE);

        // — Initialisation des systèmes annexes
        DATA_SERVICE.startAutoSave();
        DATA_SERVICE.init(GAME_NODE);

        CombatListener.init(GAME_NODE);
        ItemEventsGlobal.init(GAME_NODE);
        ItemEventsCustom.init(GAME_NODE);

        // — Commandes et items
        CommandRegister.init();
        ItemBootstrap.init();
    }

    /* ------------------------------------------------------------------ */
    /* Interdiction d’instancier cette classe                              */
    /* ------------------------------------------------------------------ */
    private NodesManagement() {
    }

    /**
     * Force le chargement de la classe (donc l’exécution du bloc static)
     * si ce n’est pas déjà fait.
     * Appelez simplement <code>NodesManagement.init();</code> dans votre
     * bootstrap principal.
     */
    public static void init() {
        // No-op : tout est déjà fait dans le bloc static.
    }

    /**
     * Accès au service de gestion des données joueur.
     */
    public static PlayerDataService getDataService() {
        return DATA_SERVICE;
    }
}

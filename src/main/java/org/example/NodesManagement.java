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
import org.example.commands.NpcInteractionCommand;
import org.example.mmo.combat.CombatListener;
import org.example.data.JsonPlayerDataRepository;
import org.example.data.PlayerDataService;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.dev.QuestEntitySpawner;
import org.example.mmo.item.ItemBootstrap;
import org.example.mmo.item.ItemEventsCustom;
import org.example.mmo.item.ItemEventsGlobal;
import org.example.mmo.npc.NpcBootstrap;
import org.example.mmo.player.PlayerQuestListener;
import org.example.mmo.quest.registry.QuestBootstrap;
import org.example.mmo.quest.QuestManager;

import java.util.Objects;

public final class NodesManagement {

    private static final GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();
    private static final EventNode<Event> GAME_NODE = EventNode.all("gameNode");
    private static final EventNode<PlayerEvent> PLAYER_NODE = EventNode.type("playerNode", EventFilter.PLAYER);
    private static final EventNode<EntityEvent> ENTITY_NODE = EventNode.type("entityNode", EventFilter.ENTITY);
    private static final EventNode<InventoryEvent> INVENTORY_NODE = EventNode.type("inventoryNode", EventFilter.INVENTORY);

    private static final PlayerDataService DATA_SERVICE = new PlayerDataService(new JsonPlayerDataRepository());

    static {
        GAME_NODE.addChild(PLAYER_NODE);
        GAME_NODE.addChild(ENTITY_NODE);
        GAME_NODE.addChild(INVENTORY_NODE);
        GLOBAL_EVENTS.addChild(GAME_NODE);

        DATA_SERVICE.startAutoSave();
        DATA_SERVICE.init(GAME_NODE);

        DamageTracker.init();
        CombatListener.init(GAME_NODE);
        ItemEventsGlobal.init(GAME_NODE);
        ItemEventsCustom.init(GAME_NODE);
        QuestManager.init(GAME_NODE);
        PlayerQuestListener.init(PLAYER_NODE);

        CommandRegister.init();
        MinecraftServer.getCommandManager().register(new NpcInteractionCommand());

        ItemBootstrap.init();
        QuestBootstrap.init();
        NpcBootstrap.init();

        QuestEntitySpawner.spawnPersistentEntities();
    }

    private NodesManagement() {}

    public static void init() {}

    public static PlayerDataService getDataService() {
        return DATA_SERVICE;
    }
}

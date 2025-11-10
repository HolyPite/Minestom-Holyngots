package org.example.bootstrap;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import org.example.mmo.commands.CommandRegister;
import org.example.mmo.commands.NpcDialogCommand;
import org.example.mmo.commands.NpcInteractionCommand;
import org.example.mmo.player.data.JsonPlayerDataRepository;
import org.example.mmo.player.data.PlayerDataService;
import org.example.mmo.combat.CombatListener;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.combat.ui.CombatBossBarService;
import org.example.mmo.dev.QuestEntitySpawner;
import org.example.mmo.inventory.InventoryListener;
import org.example.mmo.item.ItemBootstrap;
import org.example.mmo.item.ItemEventsCustom;
import org.example.mmo.item.ItemEventsGlobal;
import org.example.mmo.item.power.PowerBootstrap;
import org.example.mmo.npc.NpcBootstrap;
import org.example.mmo.mob.MobAiService;
import org.example.mmo.mob.MobBootstrap;
import org.example.mmo.mob.MobSpawnService;
import org.example.mmo.mob.zone.MobSpawningZoneService;
import org.example.mmo.mob.zone.MobZoneBootstrap;
import org.example.mmo.npc.dialog.NpcDialogService;
import org.example.mmo.player.PlayerQuestListener;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.registry.QuestBootstrap;

import java.util.Objects;

/**
 * Configures the Minestom event graph and registers gameplay systems.
 */
public final class GameLifecycle {

    private final EventNode<Event> gameNode = EventNode.all("gameNode");
    private final EventNode<PlayerEvent> playerNode;
    private final EventNode<EntityEvent> entityNode;
    private final EventNode<InventoryEvent> inventoryNode;
    private final PlayerDataService playerDataService;
    private final MobSpawnService mobSpawnService = new MobSpawnService();
    private final MobSpawningZoneService mobSpawningZoneService = new MobSpawningZoneService(mobSpawnService);

    public GameLifecycle(InstanceRegistry instances) {
        this.playerDataService = new PlayerDataService(new JsonPlayerDataRepository(), instances);
        this.playerNode = EventNode.event("playerNode", EventFilter.PLAYER, event -> {
            Instance inst = event.getPlayer().getInstance();
            return inst != null && instances.isGameInstance(inst);
        });
        this.entityNode = EventNode.event("entityNode", EventFilter.ENTITY, event -> {
            Instance inst = event.getEntity().getInstance();
            return inst != null && instances.isGameInstance(inst);
        });
        this.inventoryNode = EventNode.event("inventoryNode", EventFilter.INVENTORY, event -> event.getInventory().getViewers().stream()
                .map(Entity::getInstance)
                .filter(Objects::nonNull)
                .anyMatch(instances::isGameInstance));

        gameNode.addChild(playerNode);
        gameNode.addChild(entityNode);
        gameNode.addChild(inventoryNode);

        GlobalEventHandler globalEvents = MinecraftServer.getGlobalEventHandler();
        globalEvents.addChild(gameNode);

        playerDataService.startAutoSave();
        playerDataService.init(gameNode);

        registerGameplay(instances);
    }

    private void registerGameplay(InstanceRegistry instances) {
        DamageTracker.init();
        MobAiService.init(entityNode);
        mobSpawningZoneService.init(entityNode);
        CombatBossBarService.init(gameNode, playerNode);
        CombatListener.init(gameNode);
        PowerBootstrap.init();
        ItemEventsGlobal.init(gameNode);
        ItemEventsCustom.init(gameNode);
        QuestManager.init(gameNode);
        NpcDialogService.init(playerNode);
        PlayerQuestListener.init(playerNode);
        InventoryListener.init(inventoryNode);

        CommandRegister.init();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new NpcInteractionCommand());
        commandManager.register(new NpcDialogCommand());

        ItemBootstrap.init();
        QuestBootstrap.init();
        NpcBootstrap.init();
        MobBootstrap.init();
        MobZoneBootstrap.init(instances, mobSpawningZoneService);

        QuestEntitySpawner.spawnPersistentEntities(instances);
    }

    public EventNode<Event> gameNode() {
        return gameNode;
    }

    public EventNode<PlayerEvent> playerNode() {
        return playerNode;
    }

    public EventNode<InventoryEvent> inventoryNode() {
        return inventoryNode;
    }

    public PlayerDataService playerDataService() {
        return playerDataService;
    }

    public MobSpawnService mobSpawnService() {
        return mobSpawnService;
    }

    public MobSpawningZoneService mobSpawningZoneService() {
        return mobSpawningZoneService;
    }
}


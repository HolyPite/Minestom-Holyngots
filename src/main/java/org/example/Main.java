package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.example.mmo.items.datas.Stats;
import org.example.utils.Explosion.ExplosionSupplierUtils;
import org.example.mmo.combats.CombatListener;
import org.example.commands.CommandRegister;
import org.example.mmo.items.ItemEventsCustom;
import org.example.mmo.items.ItemEventsGlobal;
import org.example.mmo.items.ItemBootstrap;
import org.example.mmo.items.itemsList.DEV.StatsGrimoire;
import org.example.mmo.data.JsonPlayerDataRepository;
import org.example.mmo.data.PlayerDataService;

import java.time.Duration;
import java.util.Set;
//@SuppressWarnings("unchecked")

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        InstanceContainer gameInstance1 = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("main_land"));
        gameInstance1.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        gameInstance1.setChunkSupplier(LightingChunk::new);
        gameInstance1.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);

        InstanceContainer gameInstance2 = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("main_land"));
        gameInstance2.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        gameInstance2.setChunkSupplier(LightingChunk::new);
        gameInstance2.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);

        InstanceContainer buildInstance1 = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("build_world"));
        buildInstance1.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        buildInstance1.setChunkSupplier(LightingChunk::new);
        buildInstance1.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);




        // Type d'instance
        Set<Instance> GAME_INSTANCES = Set.of(gameInstance1, gameInstance2);

        // Instance Node
        GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();

        EventNode<Event> gameNode = EventNode.all("gameNode");

        // Sous Node
        EventNode<PlayerEvent> playerNode = EventNode.event("playerNode", EventFilter.PLAYER,
                e -> GAME_INSTANCES.contains(e.getPlayer().getInstance())
        );

        EventNode<EntityEvent> entityNode = EventNode.event("entityNode", EventFilter.ENTITY,
                e -> GAME_INSTANCES.contains(e.getEntity().getInstance())
        );

        EventNode<InventoryEvent> inventoryNode = EventNode.event("inventoryNode", EventFilter.INVENTORY,
                e -> {
                    Instance inst = e.getInventory().getViewers().stream().findFirst().get().getInstance();
                    return GAME_INSTANCES.contains(inst);
                }
        );

        // Attach Child/Parent
        gameNode.addChild(playerNode);
        gameNode.addChild(entityNode);
        gameNode.addChild(inventoryNode);

        events.addChild(gameNode);


        //Initialisation des mécaniques
        PlayerDataService dataService = new PlayerDataService(new JsonPlayerDataRepository());

        dataService.startAutoSave();

        dataService.init(gameNode);
        CombatListener.init(gameNode);
        ItemEventsGlobal.init(gameNode);
        ItemEventsCustom.init(gameNode);

        CommandRegister.init();
        ItemBootstrap.init();




        // Gestion du spawn du joueur
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(gameInstance1);
            player.setRespawnPoint(new Pos(0, 42, 0));

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(gameInstance1,new Pos(0, 42, 0));
            
            player.getInventory().setItemStack( 17,StatsGrimoire.ITEM.toItemStack());

            player.scheduler().buildTask(() -> {
                Stats.refresh(player);
            }).delay(TaskSchedule.tick(1)).schedule();
        });




        var scheduler = MinecraftServer.getSchedulerManager();

        //Save worlds
        scheduler.buildShutdownTask( () -> {
            System.out.println("Server shutting down... Saving chunk");
            MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
        });

        scheduler.buildTask( () -> {
            System.out.println("Server autosave... Saving chunk");
            MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
        })      .repeat(Duration.ofSeconds(30))
                .delay(Duration.ofMinutes(1))
                .schedule();


        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }

}

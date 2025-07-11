package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.example.items.datas.Stats;
import org.example.utils.Explosion.ExplosionSupplierUtils;
import org.example.combats.CombatListener;
import org.example.commands.CommandRegister;
import org.example.items.ItemEventsCustom;
import org.example.items.ItemEventsGlobal;
import org.example.items.ItemBootstrap;
import org.example.items.itemsList.DEV.StatsGrimoire;
import org.example.data.JsonPlayerDataRepository;
import org.example.data.PlayerDataService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("main_land"));
        instance.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instance.setChunkSupplier(LightingChunk::new);
        instance.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);

        GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();

        PlayerDataService dataService = new PlayerDataService(new JsonPlayerDataRepository());
        dataService.init(events);
        dataService.startAutoSave();

        // Gestion du spawn du joueur et du stack de laine rouge
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0, 42, 0));

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(instance,new Pos(0, 42, 0));
            
            player.getInventory().setItemStack( 17,StatsGrimoire.ITEM.toItemStack());

            player.scheduler().buildTask(() -> {
                Stats.refresh(player);
            }).delay(TaskSchedule.tick(1)).schedule();
        });

        ItemEventsGlobal.init(events);
        ItemEventsCustom.init(events);
        CombatListener.init(events);
        CommandRegister.init();
        ItemBootstrap.init();

        MojangAuth.init();

        //System.out.println("Items chargés : " + ItemRegistry.all().keySet());

        var scheduler = MinecraftServer.getSchedulerManager();

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

        server.start("0.0.0.0", 25565);
    }

}

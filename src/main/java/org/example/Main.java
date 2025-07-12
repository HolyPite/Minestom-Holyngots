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
import java.util.Objects;
import java.util.Set;
//@SuppressWarnings("unchecked")

public class Main {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler GLOBAL_EVENTS = MinecraftServer.getGlobalEventHandler();


        //instance declaration
        InstancesInit.init();

        //NodeInit
        NodesManagement.init();


        // Gestion du spawn du joueur
        GLOBAL_EVENTS.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(InstancesInit.GAME_INSTANCE_1);
            player.setRespawnPoint(new Pos(0, 42, 0));

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(InstancesInit.GAME_INSTANCE_1,new Pos(0, 42, 0));

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
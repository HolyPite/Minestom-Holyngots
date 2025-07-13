package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;
import org.example.data.TeleportUtils;


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

            PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(),InstancesInit.GAME_INSTANCES);

            TeleportUtils.Target target = TeleportUtils.lastPositionInInstanceGroup(player,InstancesInit.GAME_INSTANCES);

            event.setSpawningInstance(target.instance());
            player.setRespawnPoint(target.pos());

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(target.instance(),target.pos());

        });


        //Sauvegarde des mondes
        InstancesSaving.init();

        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }

}
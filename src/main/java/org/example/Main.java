package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;

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

            PlayerData data = PlayerDataUtils.loadLastData(player.getUuid());
            InstanceContainer target = InstancesInit.instance_get(data.lastInstance);
            if (target == null) {
                target = InstancesInit.GAME_INSTANCE_1;
            }
            Pos spawn = data.position != null ? data.position : new Pos(0, 42, 0);

            event.setSpawningInstance(target);
            player.setRespawnPoint(spawn);

            EntityCreature warden = new EntityCreature(EntityType.WARDEN);
            warden.setInstance(target, spawn);

        });

        //Sauvegarde des mondes
        InstancesSaving.init();

        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }

}
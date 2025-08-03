package org.example;


import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.item.ItemStack;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;
import org.example.data.teleport.TeleportUtils;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.quest.QuestTags;


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
            warden.setTag(QuestTags.ID,"guide");
            warden.setInstance(target.instance(),target.pos());
        });


        //Sauvegarde des mondes
        InstancesSaving.init();

        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }

}
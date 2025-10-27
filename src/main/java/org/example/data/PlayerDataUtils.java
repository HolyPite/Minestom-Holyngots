package org.example.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.example.InstancesInit;
import org.example.data.data_class.ItemData;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public final class PlayerDataUtils {
    private static final Path FOLDER = Paths.get("playerdata");
    private static final JsonPlayerDataRepository REPOSITORY = new JsonPlayerDataRepository();

    private PlayerDataUtils() {}

    public static PlayerData loadLastData(UUID uuid, Set<Instance> group) {
        String name = InstancesInit.instance_type_name_get(group);
        Path file = FOLDER.resolve(name + "/" + uuid + ".json");
        PlayerData data;
        if (Files.exists(file)) {
            data = REPOSITORY.load(uuid, group);
        } else {
            data = new PlayerData(uuid);
        }

        // FIX: Use getOnlinePlayerByUuid
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player != null) {
            player.getInventory().clear();
            for (ItemData slotData : data.inventory) {
                if (slotData != null && slotData.itemId != null) {
                    GameItem gameItem = ItemRegistry.byId(slotData.itemId);
                    if (gameItem != null) {
                        ItemStack itemStack = gameItem.toItemStack().withAmount(slotData.amount);
                        player.getInventory().setItemStack(slotData.slot, itemStack);
                    }
                }
            }
        }

        return data;
    }
}

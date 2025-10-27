package org.example.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.example.InstancesInit;
import org.example.data.data_class.ItemData;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.ItemUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class JsonPlayerDataRepository implements PlayerDataRepository {
    private final Path folder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonPlayerDataRepository() {
        this(Paths.get("playerdata"));
    }

    public JsonPlayerDataRepository(Path folder) {
        this.folder = folder;
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create data folder", e);
        }
    }

    @Override
    public PlayerData load(UUID playerId, Set<Instance> inst_type) {
        Path file = folder.resolve( InstancesInit.instance_type_name_get(inst_type) + "/" + playerId.toString() + ".json");
        if (!Files.exists(file)) {
            return new PlayerData(playerId);
        }
        try (Reader r = Files.newBufferedReader(file)) {
            PlayerData data = gson.fromJson(r, PlayerData.class);
            if (data == null) data = new PlayerData(playerId);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(PlayerData data, Set<Instance> instanceType) {
        // FIX: Use getOnlinePlayerByUuid
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(data.uuid);
        if (player != null) {
            data.inventory = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack itemStack = player.getInventory().getItemStack(i);
                String itemId = ItemUtils.getId(itemStack);
                data.inventory.add(new ItemData(itemId, itemStack.amount(), i));
            }
        }

        String folderName = InstancesInit.instance_type_name_get(instanceType);
        Path file = folder.resolve(folderName + "/" + data.uuid + ".json");
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) {
                gson.toJson(data, w);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

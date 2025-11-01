package org.example.mmo.player.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.instance.Instance;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public PlayerData load(UUID playerId, Set<Instance> instanceType) {
        Path file = folder.resolve(GameContext.get().instances().nameOfGroup(instanceType) + "/" + playerId + ".json");
        if (!Files.exists(file)) {
            return new PlayerData(playerId);
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            PlayerData data = gson.fromJson(reader, PlayerData.class);
            return data != null ? data : new PlayerData(playerId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(PlayerData data, Set<Instance> instanceType) {
        String folderName = GameContext.get().instances().nameOfGroup(instanceType);
        Path file = folder.resolve(folderName + "/" + data.uuid + ".json");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

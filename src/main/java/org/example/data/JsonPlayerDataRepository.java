package org.example.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Stores player data as one JSON file per player in the {@code playerdata} directory.
 */
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
    public PlayerData load(UUID playerId) {
        Path file = folder.resolve(playerId.toString() + ".json");
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
    public void save(PlayerData data) {
        Path file = folder.resolve(data.uuid.toString() + ".json");
        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(data, w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

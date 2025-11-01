package org.example.mmo.player.data;

import net.minestom.server.instance.Instance;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public final class PlayerDataUtils {
    private static final Path FOLDER = Paths.get("playerdata");
    private static final JsonPlayerDataRepository REPOSITORY = new JsonPlayerDataRepository();

    private PlayerDataUtils() {
    }

    public static PlayerData loadLastData(UUID uuid, Set<Instance> group) {
        String name = GameContext.get().instances().nameOfGroup(group);
        Path file = FOLDER.resolve(name + "/" + uuid + ".json");
        if (Files.exists(file)) {
            return REPOSITORY.load(uuid, group);
        }
        return new PlayerData(uuid);
    }
}

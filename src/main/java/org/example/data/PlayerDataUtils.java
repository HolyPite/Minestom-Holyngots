package org.example.data;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.example.InstancesInit;
import org.example.data.data_class.PlayerData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Utility methods for loading persistent player data.
 */
public final class PlayerDataUtils {
    private static final Path FOLDER = Paths.get("playerdata");
    private static final JsonPlayerDataRepository REPOSITORY = new JsonPlayerDataRepository();

    private PlayerDataUtils() {}

    /**
     * Loads the last saved data for the given player, if it exists.
     * If no stored data is found, a new {@link PlayerData} instance is returned.
     */
    public static PlayerData loadLastData(UUID uuid, Set<Instance> group) {
        String name = InstancesInit.instance_type_name_get(group);
        Path file = FOLDER.resolve(name + "/" + uuid + ".json");
        if (Files.exists(file)) {
            return REPOSITORY.load(uuid, group);
        }
        return new PlayerData(uuid);
    }

}

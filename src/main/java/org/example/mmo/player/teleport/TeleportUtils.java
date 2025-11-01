package org.example.mmo.player.teleport;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.player.data.PlayerDataUtils;

/**
 * Utilities to restore and move players within logical instance groups.
 */
public final class TeleportUtils {

    private TeleportUtils() {
    }

    public record Target(Pos pos, Instance instance) { }

    public static Target lastPositionInInstanceGroup(Player player, Set<Instance> group) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(group, "group");

        PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), group);

        Instance target = null;
        if (data != null && data.lastInstance != null) {
            target = GameContext.get().instances().byName(data.lastInstance);
        }

        if (target == null || !group.contains(target)) {
            player.sendMessage("Aucune derni��re instance trouv��e dans �� " + GameContext.get().instances().nameOfGroup(group) + " ��.");
            Iterator<Instance> it = group.iterator();
            if (it.hasNext()) {
                target = it.next();
            }
            if (target instanceof InstanceContainer container) {
                player.sendMessage("Envoi vers le monde par d��faut : " + GameContext.get().instances().nameOf(container));
            }
        }

        Pos pos = (data != null && data.position != null)
                ? data.position
                : new Pos(0, 42, 0);

        return new Target(pos, target);
    }

    public static Target teleportToInstanceGroup(Player player, Set<Instance> group) {
        GameContext.get().playerDataService().savePlayer(player);

        Target target = lastPositionInInstanceGroup(player, group);
        player.setInstance(target.instance(), target.pos());
        return target;
    }

    public static Target teleportToInstance(Player player, Instance instance) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(instance, "instance");

        Set<Instance> group = GameContext.get().instances().groupFor(instance);

        if (group == null) {
            player.sendMessage("Cette instance ne fait partie d'aucun groupe connu.");
            return null;
        }

        GameContext.get().playerDataService().savePlayer(player);

        PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), group);

        Pos pos = (data != null && data.position != null)
                ? data.position
                : new Pos(0, 42, 0);

        player.setInstance(instance, pos);
        return new Target(pos, instance);
    }
}

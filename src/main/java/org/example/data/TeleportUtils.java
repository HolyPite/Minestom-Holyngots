package org.example.teleport;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.example.InstancesInit;
import org.example.NodesManagement;
import org.example.data.PlayerDataUtils;
import org.example.data.data_class.PlayerData;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static org.example.InstancesInit.instance_type_get;

/**
 * Utilitaires de téléportation entre groupes d’instances.
 */
public final class TeleportUtils {

    private TeleportUtils() {
    }

    /* ------------------------------------------------------------------ */
    /*  Record pour retourner un couple (Instance, Pos)                    */
    /* ------------------------------------------------------------------ */
    public record Target(Pos pos, Instance instance) {}

    /**
     * Cherche la dernière position sauvegardée dans le groupe {@code set}
     * et renvoie un objet {@link Target}.
     * <p>
     * S’il n’existe aucune position valide, on utilise le premier monde du
     * groupe et la position par défaut (0 ;42 ;0).
     */
    public static Target lastPositionInInstanceGroup(Player player, Set<Instance> set) {

        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(set,   "set");

        PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), set);

        /* -------- Recherche de l’instance cible -------- */
        Instance target = null;
        if (data != null && data.lastInstance != null) {
            target = InstancesInit.instance_get(data.lastInstance);
        }

        if (target == null || !set.contains(target)) {
            player.sendMessage("Aucune dernière instance trouvée dans « " + InstancesInit.instance_type_name_get(set) + " ».");
            // Choix du premier élément du Set comme "monde par défaut"
            Iterator<Instance> it = set.iterator();
            if (it.hasNext()) target = it.next();
            player.sendMessage("Envoi vers le monde par défaut : " + InstancesInit.instance_name_get((InstanceContainer) target));
        }

        /* -------- Position cible -------- */
        Pos pos = (data != null && data.position != null)
                ? data.position
                : new Pos(0, 42, 0);

        return new Target(pos, target);
    }

    /**
     * Téléporte le joueur dans le groupe d’instances désigné
     * (raccourci pratique qui appelle {@link #lastPositionInInstanceGroup})
     * et renvoie la cible réellement utilisée.
     */
    public static Target teleportToInstanceGroup(Player player, Set<Instance> set) {
        // Sauvegarder la position actuelle avant le changement d'instance
        NodesManagement.getDataService().savePlayer(player);

        Target target = lastPositionInInstanceGroup(player, set);
        player.setInstance(target.instance(), target.pos());
        return target;
    }

    /**
     * Téléporte le joueur dans une {@link Instance} donnée,
     * à sa dernière position connue dans le groupe d’instances correspondant.
     *
     * <p>Si aucune position n’est trouvée, utilise une position par défaut (0;42;0).</p>
     *
     * @param player    le joueur à téléporter
     * @param instance  l’instance cible (doit appartenir à un groupe connu)
     * @return la cible utilisée (position + instance), ou {@code null} si l’instance n’est dans aucun groupe
     */
    public static Target teleportToInstance(Player player, Instance instance) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(instance, "instance");

        // Trouver le groupe auquel appartient cette instance
        Set<Instance> group = instance_type_get(instance);

        if (group == null) {
            player.sendMessage("Cette instance ne fait partie d’aucun groupe connu.");
            return null;
        }

        // Charger les données du joueur dans ce groupe
        PlayerData data = PlayerDataUtils.loadLastData(player.getUuid(), group);

        // Position enregistrée ou valeur par défaut
        Pos pos = (data != null && data.position != null)
                ? data.position
                : new Pos(0, 42, 0);

        // Sauvegarder la position actuelle avant le changement d'instance
        NodesManagement.getDataService().savePlayer(player);

        player.setInstance(instance, pos);
        return new Target(pos, instance);
    }

}

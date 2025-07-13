package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.example.utils.Explosion.ExplosionSupplierUtils;

import java.util.Map;
import java.util.Set;

/**
 * Classe utilitaire chargée de créer et d’exposer les instances Minestom.
 */
public final class InstancesInit {

    /* ------------------------------------------------------------------ */
    /* Instances créées une seule fois au chargement de la classe          */
    /* ------------------------------------------------------------------ */
    public static final InstanceContainer GAME_INSTANCE_1;
    public static final InstanceContainer GAME_INSTANCE_2;
    public static final InstanceContainer BUILD_INSTANCE_1;
    public static final InstanceContainer BUILD_INSTANCE_2;

    /** Ensemble pratique pour itérer sur les instances de jeu. */
    public static final Set<Instance> GAME_INSTANCES;
    public static final Set<Instance> BUILD_INSTANCES;

    public static final Set<Set<Instance>> ALL_INSTANCES;

    /** Accès simplifié aux instances par nom. */
    public static final Map<String, InstanceContainer> INSTANCE_BY_NAME;
    public static final Map<String, Set<Instance>> INSTANCE_TYPE_BY_NAME;

    public static final Map<InstanceContainer, String> NAME_BY_INSTANCE;
    public static final Map<Set<Instance>, String> NAME_BY_INSTANCE_TYPE;

    /* ------------------------------------------------------------------ */
    /* Bloc static – exécuté une fois lorsque la classe est chargée        */
    /* ------------------------------------------------------------------ */
    static {
        GAME_INSTANCE_1 = createInstance("worlds/GAME_INSTANCE_1");
        GAME_INSTANCE_2 = createInstance("worlds/GAME_INSTANCE_2");
        BUILD_INSTANCE_1 = createInstance("worlds/BUILD_INSTANCE_1");
        BUILD_INSTANCE_2 = createInstance("worlds/BUILD_INSTANCE_2");

        GAME_INSTANCES = Set.of(GAME_INSTANCE_1, GAME_INSTANCE_2);
        BUILD_INSTANCES = Set.of(BUILD_INSTANCE_1, BUILD_INSTANCE_2);

        ALL_INSTANCES = Set.of(GAME_INSTANCES, BUILD_INSTANCES);


        INSTANCE_BY_NAME = Map.of(
                "game1", GAME_INSTANCE_1,
                "game2", GAME_INSTANCE_2,
                "build1", BUILD_INSTANCE_1,
                "build2", BUILD_INSTANCE_2
        );

        NAME_BY_INSTANCE = Map.of(
                GAME_INSTANCE_1,"game1",
                GAME_INSTANCE_2,"game2",
                BUILD_INSTANCE_1,"build1",
                BUILD_INSTANCE_2, "build2"
        );

        INSTANCE_TYPE_BY_NAME = Map.of(
                "games", GAME_INSTANCES,
                "builds", BUILD_INSTANCES
        );

        NAME_BY_INSTANCE_TYPE = Map.of(
                GAME_INSTANCES,"games",
                BUILD_INSTANCES,"builds"
                );
    }

    /**
     * Empêche l’instanciation : classe purement utilitaire.
     */
    private InstancesInit() { }

    /**
     * Méthode à appeler depuis votre bootstrap.
     * Elle force simplement le chargement de la classe, donc l’exécution
     * du bloc static ci-dessus.
     */
    public static void init() {
        /* Rien à faire : le bloc static a déjà créé les instances. */
    }

    /**
     * Retourne l'instance correspondant au nom indiqué (null si inconnue).
     */
    public static InstanceContainer instance_get(String name) {
        if (name == null) return null;
        return INSTANCE_BY_NAME.get(name.toLowerCase());
    }

    public static String instance_name_get(InstanceContainer inst) {
        if (inst == null) return null;
        return NAME_BY_INSTANCE.get(inst);
    }

    public static Set<Instance> instance_type_get(String name) {
        if (name == null) return null;
        return INSTANCE_TYPE_BY_NAME.get(name.toLowerCase());
    }

    public static String instance_type_name_get(Set<Instance> inst) {
        if (inst == null) return null;
        return NAME_BY_INSTANCE_TYPE.get(inst);
    }

    public static Set<Instance> instance_type_get(Instance inst) {
        if (inst == null) return null;
        for (Set<Instance> group : ALL_INSTANCES) {
            if (group.contains(inst)) {
                return group;
            }
        }
        return null; // instance inconnue
    }

    public static String instance_group_name_get(Instance inst) {
        return instance_type_name_get(instance_type_get(inst));
    }


    /* ------------------------------------------------------------------ */
    /* Méthode utilitaire pour éviter la duplication de code              */
    /* ------------------------------------------------------------------ */
    private static InstanceContainer createInstance(String worldFolder) {
        InstanceContainer instance = MinecraftServer.getInstanceManager()
                .createInstanceContainer(new AnvilLoader(worldFolder));

        instance.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instance.setChunkSupplier(LightingChunk::new);
        instance.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);

        return instance;
    }
}

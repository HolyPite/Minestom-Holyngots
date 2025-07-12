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

    /** Accès simplifié aux instances par nom. */
    public static final Map<String, InstanceContainer> INSTANCE_BY_NAME;

    /** Ensemble pratique pour itérer sur les instances de jeu. */
    public static final Set<Instance> GAME_INSTANCES;

    /* ------------------------------------------------------------------ */
    /* Bloc static – exécuté une fois lorsque la classe est chargée        */
    /* ------------------------------------------------------------------ */
    static {
        GAME_INSTANCE_1 = createInstance("worlds/GAME_INSTANCE_1");
        GAME_INSTANCE_2 = createInstance("worlds/GAME_INSTANCE_2");
        BUILD_INSTANCE_1 = createInstance("worlds/BUILD_INSTANCE_1");

        GAME_INSTANCES = Set.of(GAME_INSTANCE_1, GAME_INSTANCE_2);

        INSTANCE_BY_NAME = Map.of(
                "game1", GAME_INSTANCE_1,
                "game2", GAME_INSTANCE_2,
                "build1", BUILD_INSTANCE_1
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
    public static InstanceContainer get(String name) {
        if (name == null) return null;
        return INSTANCE_BY_NAME.get(name.toLowerCase());
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

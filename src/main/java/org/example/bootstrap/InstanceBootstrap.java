package org.example.bootstrap;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.example.utils.Explosion.ExplosionSupplierUtils;

/**
 * Responsible for creating the Minestom instances that compose the game worlds.
 */
public final class InstanceBootstrap {

    public InstanceRegistry createRegistry() {
        InstanceContainer gameInstance1 = createInstance("worlds/GAME_INSTANCE_1");
        InstanceContainer gameInstance2 = createInstance("worlds/GAME_INSTANCE_2");
        InstanceContainer buildInstance1 = createInstance("worlds/BUILD_INSTANCE_1");
        InstanceContainer buildInstance2 = createInstance("worlds/BUILD_INSTANCE_2");

        return new InstanceRegistry(gameInstance1, gameInstance2, buildInstance1, buildInstance2);
    }

    private InstanceContainer createInstance(String worldFolder) {
        InstanceContainer instance = MinecraftServer.getInstanceManager()
                .createInstanceContainer(new AnvilLoader(worldFolder));

        instance.setGenerator(u -> u.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instance.setChunkSupplier(LightingChunk::new);
        instance.setExplosionSupplier(ExplosionSupplierUtils.DEFAULT);
        instance.enableAutoChunkLoad(true);

        return instance;
    }
}

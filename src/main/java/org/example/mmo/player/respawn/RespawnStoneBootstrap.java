package org.example.mmo.player.respawn;

public final class RespawnStoneBootstrap {

    private RespawnStoneBootstrap() {
    }

    public static RespawnStoneRegistry init() {
        RespawnStoneRegistry registry = new RespawnStoneRegistry();
        RespawnStoneDefinitions.registerDefaults(registry);
        return registry;
    }
}

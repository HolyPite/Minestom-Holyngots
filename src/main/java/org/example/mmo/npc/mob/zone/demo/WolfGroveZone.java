package org.example.mmo.npc.mob.zone.demo;

import net.minestom.server.coordinate.Pos;
import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.mob.demo.ForestWolfMob;
import org.example.mmo.npc.mob.zone.MobSpawningZone;
import org.example.mmo.npc.mob.zone.MobSpawningZoneService;

import java.time.Duration;
import java.util.List;

public final class WolfGroveZone {

    public static final String ID = "wolf_grove";
    public static final String NAME = "Clairière des loups";

    private WolfGroveZone() {
    }

    public static void register(InstanceRegistry instances, MobSpawningZoneService service) {
        var instance = instances.gameInstance1();
        MobSpawningZone zone = MobSpawningZone.create(
                ID,
                NAME,
                instance,
                new Pos(52.5, 65, -28.5),
                14.0,
                List.of(ForestWolfMob.ID),
                List.of(5),
                Duration.ofSeconds(25)
        );
        service.registerZone(zone);
    }
}

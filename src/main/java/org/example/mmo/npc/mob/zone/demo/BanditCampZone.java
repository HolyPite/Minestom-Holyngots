package org.example.mmo.npc.mob.zone.demo;

import net.minestom.server.coordinate.Pos;
import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.mob.demo.BanditArcherMob;
import org.example.mmo.npc.mob.demo.BanditSkirmisherMob;
import org.example.mmo.npc.mob.zone.MobSpawningZone;
import org.example.mmo.npc.mob.zone.MobSpawningZoneService;

import java.time.Duration;
import java.util.List;

public final class BanditCampZone {

    public static final String ID = "bandit_camp";
    public static final String NAME = "Campement des bandits";

    private BanditCampZone() {
    }

    public static void register(InstanceRegistry instances, MobSpawningZoneService service) {
        var instance = instances.gameInstance2();
        MobSpawningZone zone = MobSpawningZone.create(
                ID,
                NAME,
                instance,
                new Pos(-36.0, 65, 18.0),
                18.0,
                List.of(BanditSkirmisherMob.ID, BanditArcherMob.ID),
                List.of(6, 3),
                Duration.ofSeconds(35)
        );
        service.registerZone(zone);
    }
}

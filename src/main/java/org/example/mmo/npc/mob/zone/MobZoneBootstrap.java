package org.example.mmo.npc.mob.zone;

import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.mob.zone.demo.BanditCampZone;
import org.example.mmo.npc.mob.zone.demo.WolfGroveZone;

public final class MobZoneBootstrap {

    private MobZoneBootstrap() {
    }

    public static void init(InstanceRegistry instances, MobSpawningZoneService service) {
        WolfGroveZone.register(instances, service);
        BanditCampZone.register(instances, service);
    }
}

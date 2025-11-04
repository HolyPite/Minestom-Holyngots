package org.example.mmo.mob.zone;

import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.mob.zone.zones.MobZoneDefinitions;

public final class MobZoneBootstrap {

    private MobZoneBootstrap() {
    }

    public static void init(InstanceRegistry instances, MobSpawningZoneService service) {
        MobZoneRegistrations.registerForAllInstances(MobZoneDefinitions.ZONES, instances, service);
    }
}




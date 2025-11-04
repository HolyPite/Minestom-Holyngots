package org.example.mmo.npc.mob.zone;

import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.mob.zone.definition.MobZoneDefinitions;

public final class MobZoneBootstrap {

    private MobZoneBootstrap() {
    }

    public static void init(InstanceRegistry instances, MobSpawningZoneService service) {
        MobZoneRegistrations.registerForAllInstances(MobZoneDefinitions.ZONES, instances, service);
    }
}




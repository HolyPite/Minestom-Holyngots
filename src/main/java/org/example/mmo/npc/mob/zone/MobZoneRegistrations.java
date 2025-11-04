package org.example.mmo.npc.mob.zone;

import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.mob.zone.definition.MobZoneDefinition;

import java.util.Collection;

public final class MobZoneRegistrations {

    private MobZoneRegistrations() {
    }

    public static void registerForAllInstances(Collection<MobZoneDefinition> definitions,
                                                InstanceRegistry instances,
                                                MobSpawningZoneService service) {
        definitions.forEach(definition -> registerForAllInstances(definition, instances, service));
    }

    public static void registerForAllInstances(MobZoneDefinition definition,
                                                InstanceRegistry instances,
                                                MobSpawningZoneService service) {
        instances.instanceByName().forEach((name, container) -> {
            if (!name.startsWith("game")) {
                return;
            }
            MobSpawningZone zone = MobSpawningZone.create(
                    definition.id() + "_" + name,
                    definition.displayName() + " (" + name + ")",
                    container,
                    definition.center(),
                    definition.radius(),
                    definition.mobIds(),
                    definition.maxAlive(),
                    definition.respawnDelay()
            );
            service.registerZone(zone);
        });
    }
}

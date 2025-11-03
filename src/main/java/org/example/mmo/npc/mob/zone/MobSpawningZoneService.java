package org.example.mmo.npc.mob.zone;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.EventNode;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.example.mmo.npc.mob.MobInstance;
import org.example.mmo.npc.mob.MobSpawnService;
import org.example.mmo.npc.mob.zone.MobSpawningZone.ZoneSlot;
import org.example.mmo.npc.mob.zone.MobSpawningZone.ZoneSpawn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains spawning zones and orchestrates respawn logic.
 */
public final class MobSpawningZoneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobSpawningZoneService.class);

    private final MobSpawnService spawnService;
    private final Map<String, MobSpawningZone> zones = new ConcurrentHashMap<>();
    private final Map<UUID, ZoneHandle> mobIndex = new ConcurrentHashMap<>();
    private Task tickTask;

    public MobSpawningZoneService(MobSpawnService spawnService) {
        this.spawnService = spawnService;
    }

    public void init(EventNode<EntityEvent> entityNode) {
        entityNode.addListener(EntityDeathEvent.class, this::handleEntityDeath);
        tickTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::tickZones)
                .delay(TaskSchedule.seconds(1))
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        zones.clear();
        mobIndex.clear();
    }

    public void registerZone(MobSpawningZone zone) {
        zones.put(zone.id(), zone);
        zone.initialise(System.currentTimeMillis());
        LOGGER.info("Registered mob spawning zone {} ({})", zone.id(), zone.name());
    }

    public Optional<MobSpawningZone> getZone(String id) {
        return Optional.ofNullable(zones.get(id));
    }

    public Collection<MobSpawningZone> zones() {
        return zones.values();
    }

    public void unregisterZone(String id) {
        MobSpawningZone removed = zones.remove(id);
        if (removed == null) {
            return;
        }
        mobIndex.entrySet().removeIf(entry -> {
            if (entry.getValue().zone.equals(removed)) {
                spawnService.remove(entry.getKey());
                return true;
            }
            return false;
        });
        LOGGER.info("Unregistered mob spawning zone {} ({})", removed.id(), removed.name());
    }

    private void tickZones() {
        long now = System.currentTimeMillis();
        for (MobSpawningZone zone : zones.values()) {
            for (ZoneSpawn spawn : zone.tick(spawnService, now)) {
                MobInstance instance = spawn.mobInstance();
                mobIndex.put(instance.entityUuid(), new ZoneHandle(zone, spawn.slot()));
            }
        }
    }

    private void handleEntityDeath(EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUuid();
        ZoneHandle handle = mobIndex.remove(uuid);
        if (handle == null) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean handled = handle.zone.handleDeath(uuid, now);
        spawnService.remove(uuid);
        if (!handled) {
            LOGGER.debug("Mob {} died outside expected zone {} tracking", uuid, handle.zone.id());
        }
    }

    private record ZoneHandle(MobSpawningZone zone, ZoneSlot slot) {
    }
}

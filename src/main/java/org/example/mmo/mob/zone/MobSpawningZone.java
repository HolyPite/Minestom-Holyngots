package org.example.mmo.mob.zone;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobInstance;
import org.example.mmo.mob.MobRegistry;
import org.example.mmo.mob.MobSpawnService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a hunting area that maintains a population of mobs.
 */
public final class MobSpawningZone {

    private final String id;
    private final String name;
    private final Instance instance;
    private final Pos center;
    private final double radius;
    private final long respawnDelayMillis;
    private final List<ZoneSlot> slots;

    public MobSpawningZone(String id,
                           String name,
                           Instance instance,
                           Pos center,
                           double radius,
                           List<ZoneSlot> slots,
                           Duration respawnDelay) {
        this.id = id;
        this.name = name;
        this.instance = instance;
        this.center = center;
        this.radius = radius;
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
        this.respawnDelayMillis = respawnDelay.toMillis();
    }

    public static MobSpawningZone create(String id,
                                         String name,
                                         Instance instance,
                                         Pos center,
                                         double radius,
                                         List<String> mobIds,
                                         List<Integer> maxAlive,
                                         Duration respawnDelay) {
        if (mobIds.size() != maxAlive.size()) {
            throw new IllegalArgumentException("Mob id list and max alive list must have the same size");
        }
        List<ZoneSlot> slots = new ArrayList<>();
        for (int i = 0; i < mobIds.size(); i++) {
            String archetypeId = mobIds.get(i);
            MobArchetype archetype = MobRegistry.get(archetypeId);
            if (archetype == null) {
                throw new IllegalArgumentException("Unknown mob archetype id: " + archetypeId);
            }
            slots.add(new ZoneSlot(archetype, maxAlive.get(i)));
        }
        return new MobSpawningZone(id, name, instance, center, radius, slots, respawnDelay);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Instance instance() {
        return instance;
    }

    public Pos center() {
        return center;
    }

    public double radius() {
        return radius;
    }

    public long respawnDelayMillis() {
        return respawnDelayMillis;
    }

    public List<ZoneSlot> slots() {
        return slots;
    }

    public void initialise(long now) {
        preloadChunks();
        for (ZoneSlot slot : slots) {
            slot.initialise(now);
        }
    }

    public List<ZoneSpawn> tick(MobSpawnService spawnService, long now) {
        List<ZoneSpawn> spawns = new ArrayList<>();
        for (ZoneSlot slot : slots) {
            slot.prune(spawnService, now);
            while (slot.shouldSpawn(now)) {
                Pos spawnPos = randomPosition();
                MobInstance mobInstance = spawnService.spawn(slot.archetype(), instance, spawnPos);
                slot.markSpawned(mobInstance);
                spawns.add(new ZoneSpawn(slot, mobInstance));
            }
        }
        return spawns;
    }

    public boolean handleDeath(UUID uuid, long now) {
        for (ZoneSlot slot : slots) {
            if (slot.handleDeath(uuid, now, respawnDelayMillis)) {
                return true;
            }
        }
        return false;
    }

    private Pos randomPosition() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double distance = Math.sqrt(random.nextDouble()) * radius;
        double angle = random.nextDouble() * Math.PI * 2;
        double x = center.x() + Math.cos(angle) * distance;
        double z = center.z() + Math.sin(angle) * distance;
        return new Pos(x, center.y(), z);
    }

    private void preloadChunks() {
        int padding = 2;
        int minChunkX = chunkCoordinate(center.x() - radius) - padding;
        int maxChunkX = chunkCoordinate(center.x() + radius) + padding;
        int minChunkZ = chunkCoordinate(center.z() - radius) - padding;
        int maxChunkZ = chunkCoordinate(center.z() + radius) + padding;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                instance.loadChunk(chunkX, chunkZ).join();
            }
        }
    }

    private static int chunkCoordinate(double coordinate) {
        int block = (int) Math.floor(coordinate);
        return block >> 4;
    }

    public static final class ZoneSlot {
        private final MobArchetype archetype;
        private final int maxAlive;
        private final Set<UUID> alive = new java.util.HashSet<>();
        private final PriorityQueue<Long> spawnSchedule = new PriorityQueue<>();

        public ZoneSlot(MobArchetype archetype, int maxAlive) {
            this.archetype = archetype;
            this.maxAlive = maxAlive;
        }

        public MobArchetype archetype() {
            return archetype;
        }

        public int maxAlive() {
            return maxAlive;
        }

        public Set<UUID> alive() {
            return alive;
        }

        private void initialise(long now) {
            spawnSchedule.clear();
            alive.clear();
            for (int i = 0; i < maxAlive; i++) {
                spawnSchedule.add(now);
            }
        }

        private void prune(MobSpawnService spawnService, long now) {
            Iterator<UUID> iterator = alive.iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                if (spawnService.get(uuid).flatMap(MobInstance::resolveEntity).isEmpty()) {
                    iterator.remove();
                    spawnSchedule.add(now);
                }
            }
        }

        private boolean shouldSpawn(long now) {
            return alive.size() < maxAlive && !spawnSchedule.isEmpty() && spawnSchedule.peek() <= now;
        }

        private void markSpawned(MobInstance mobInstance) {
            spawnSchedule.poll();
            alive.add(mobInstance.entityUuid());
        }

        private boolean handleDeath(UUID uuid, long now, long respawnDelayMillis) {
            if (!alive.remove(uuid)) {
                return false;
            }
            spawnSchedule.add(now + respawnDelayMillis);
            return true;
        }
    }

    public record ZoneSpawn(ZoneSlot slot, MobInstance mobInstance) {
    }
}

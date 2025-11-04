package org.example.mmo.npc.mob;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.example.mmo.npc.mob.loot.MobLootBundles;

/**
 * Simple in-memory registry for mob archetypes.
 */
public final class MobRegistry {

    private static final Map<String, MobArchetype> ARCHETYPES = new ConcurrentHashMap<>();

    private MobRegistry() {
    }

    public static void clear() {
        ARCHETYPES.clear();
    }

    public static void register(MobArchetype archetype) {
        ARCHETYPES.put(archetype.id(), archetype);
        MobLootBundles.ensureRegistered(archetype);
    }

    public static @Nullable MobArchetype get(String id) {
        return ARCHETYPES.get(id);
    }

    public static Collection<MobArchetype> values() {
        return ARCHETYPES.values();
    }

    public static boolean contains(String id) {
        return ARCHETYPES.containsKey(id);
    }
}

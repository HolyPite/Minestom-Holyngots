package org.example.mmo.item.skill;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PowerRegistry {

    private static final Map<String, Power> REGISTRY = new ConcurrentHashMap<>();

    private PowerRegistry() {}

    public static void register(String id, Power power) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Power id must not be blank");
        }
        REGISTRY.put(id, power);
    }

    public static Power resolve(String id) {
        return REGISTRY.get(id);
    }
}

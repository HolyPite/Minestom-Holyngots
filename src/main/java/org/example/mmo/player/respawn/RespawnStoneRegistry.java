package org.example.mmo.player.respawn;

import net.minestom.server.coordinate.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class RespawnStoneRegistry {

    private final List<RespawnStoneDefinition> definitions = new ArrayList<>();
    private final Map<String, RespawnStoneDefinition> byId = new ConcurrentHashMap<>();

    public void register(RespawnStoneDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (byId.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalStateException("Respawn stone already registered: " + definition.id());
        }
        definitions.add(definition);
    }

    public RespawnStoneDefinition find(Point block) {
        if (definitions.isEmpty()) {
            return null;
        }
        for (RespawnStoneDefinition definition : definitions) {
            if (definition.matches(block)) {
                return definition;
            }
        }
        return null;
    }

    public RespawnStoneDefinition byId(String id) {
        if (id == null) {
            return null;
        }
        return byId.get(id);
    }

    public List<RespawnStoneDefinition> all() {
        return Collections.unmodifiableList(definitions);
    }
}

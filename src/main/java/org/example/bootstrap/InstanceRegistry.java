package org.example.bootstrap;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable registry describing the instances that make up the game worlds.
 */
public final class InstanceRegistry {

    private final InstanceContainer gameInstance1;
    private final InstanceContainer gameInstance2;
    private final InstanceContainer buildInstance1;
    private final InstanceContainer buildInstance2;

    private final Set<Instance> gameInstances;
    private final Set<Instance> buildInstances;
    private final Set<Set<Instance>> allInstanceGroups;

    private final Map<String, InstanceContainer> instanceByName;
    private final Map<InstanceContainer, String> nameByInstance;
    private final Map<String, Set<Instance>> instanceTypeByName;
    private final Map<Set<Instance>, String> nameByInstanceType;

    InstanceRegistry(InstanceContainer gameInstance1,
                     InstanceContainer gameInstance2,
                     InstanceContainer buildInstance1,
                     InstanceContainer buildInstance2) {
        this.gameInstance1 = Objects.requireNonNull(gameInstance1, "gameInstance1");
        this.gameInstance2 = Objects.requireNonNull(gameInstance2, "gameInstance2");
        this.buildInstance1 = Objects.requireNonNull(buildInstance1, "buildInstance1");
        this.buildInstance2 = Objects.requireNonNull(buildInstance2, "buildInstance2");

        this.gameInstances = Set.of(this.gameInstance1, this.gameInstance2);
        this.buildInstances = Set.of(this.buildInstance1, this.buildInstance2);
        this.allInstanceGroups = Set.of(this.gameInstances, this.buildInstances);

        this.instanceByName = Map.of(
                "game1", this.gameInstance1,
                "game2", this.gameInstance2,
                "build1", this.buildInstance1,
                "build2", this.buildInstance2
        );

        this.nameByInstance = Map.of(
                this.gameInstance1, "game1",
                this.gameInstance2, "game2",
                this.buildInstance1, "build1",
                this.buildInstance2, "build2"
        );

        this.instanceTypeByName = Map.of(
                "games", this.gameInstances,
                "builds", this.buildInstances
        );

        this.nameByInstanceType = Map.of(
                this.gameInstances, "games",
                this.buildInstances, "builds"
        );
    }

    public InstanceContainer gameInstance1() {
        return gameInstance1;
    }

    public InstanceContainer gameInstance2() {
        return gameInstance2;
    }

    public InstanceContainer buildInstance1() {
        return buildInstance1;
    }

    public InstanceContainer buildInstance2() {
        return buildInstance2;
    }

    public Set<Instance> gameInstances() {
        return gameInstances;
    }

    public Set<Instance> buildInstances() {
        return buildInstances;
    }

    public Set<Set<Instance>> allInstanceGroups() {
        return allInstanceGroups;
    }

    public Map<String, InstanceContainer> instanceByName() {
        return Collections.unmodifiableMap(instanceByName);
    }

    public Map<InstanceContainer, String> nameByInstance() {
        return Collections.unmodifiableMap(nameByInstance);
    }

    public Map<String, Set<Instance>> instanceTypeByName() {
        return Collections.unmodifiableMap(instanceTypeByName);
    }

    public Map<Set<Instance>, String> nameByInstanceType() {
        return Collections.unmodifiableMap(nameByInstanceType);
    }

    public InstanceContainer byName(String name) {
        if (name == null) {
            return null;
        }
        return instanceByName.get(name.toLowerCase());
    }

    public String nameOf(InstanceContainer container) {
        if (container == null) {
            return null;
        }
        return nameByInstance.get(container);
    }

    public Set<Instance> groupByName(String name) {
        if (name == null) {
            return null;
        }
        return instanceTypeByName.get(name.toLowerCase());
    }

    public String nameOfGroup(Set<Instance> group) {
        if (group == null) {
            return null;
        }
        return nameByInstanceType.get(group);
    }

    public Set<Instance> groupFor(Instance instance) {
        if (instance == null) {
            return null;
        }
        for (Set<Instance> group : allInstanceGroups) {
            if (group.contains(instance)) {
                return group;
            }
        }
        return null;
    }

    public String groupNameFor(Instance instance) {
        return Optional.ofNullable(groupFor(instance))
                .map(this::nameOfGroup)
                .orElse(null);
    }

    public boolean isGameInstance(Instance instance) {
        return instance != null && gameInstances.contains(instance);
    }
}

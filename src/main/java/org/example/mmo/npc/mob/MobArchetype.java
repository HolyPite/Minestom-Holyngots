package org.example.mmo.npc.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.entity.EntityType;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.behaviour.MobBehaviourFactory;
import org.example.mmo.npc.mob.loot.MobLootTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable definition of a mob archetype (stats, equipment, behaviours, loot).
 */
public final class MobArchetype {

    private final String id;
    private final String name;
    private final Component displayName;
    private final EntityType entityType;
    private final Map<StatType, Double> stats;
    private final MobEquipment equipment;
    private final MobLootTable lootTable;
    private final Optional<MobRiderConfig> riderConfig;
    private final Set<MobTag> tags;
    private final List<MobBehaviourFactory> behaviourFactories;
    private final MobAiFactory aiFactory;
    private final MobEntityFactory entityFactory;
    private final double lootContributionThreshold;

    private MobArchetype(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.displayName = builder.displayName != null ? builder.displayName : Component.text(builder.name);
        this.entityType = builder.entityType;
        this.stats = Collections.unmodifiableMap(new EnumMap<>(builder.stats));
        this.equipment = builder.equipment != null ? builder.equipment : MobEquipment.EMPTY;
        this.lootTable = builder.lootTable != null ? builder.lootTable : MobLootTable.EMPTY;
        this.riderConfig = Optional.ofNullable(builder.riderConfig);
        this.tags = Set.copyOf(builder.tags);
        this.behaviourFactories = List.copyOf(builder.behaviourFactories);
        this.aiFactory = builder.aiFactory;
        this.entityFactory = builder.entityFactory;
        this.lootContributionThreshold = clampThreshold(builder.lootContributionThreshold);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Component displayName() {
        return displayName;
    }

    public EntityType entityType() {
        return entityType;
    }

    public Map<StatType, Double> stats() {
        return stats;
    }

    public MobEquipment equipment() {
        return equipment;
    }

    public MobLootTable lootTable() {
        return lootTable;
    }

    public Optional<MobRiderConfig> riderConfig() {
        return riderConfig;
    }

    public Set<MobTag> tags() {
        return tags;
    }

    public List<MobBehaviourFactory> behaviourFactories() {
        return behaviourFactories;
    }

    public Optional<MobAiFactory> aiFactory() {
        return Optional.ofNullable(aiFactory);
    }

    public MobEntityFactory entityFactory() {
        return entityFactory;
    }

    public double lootContributionThreshold() {
        return lootContributionThreshold;
    }

    private double clampThreshold(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.1d;
        }
        return Math.max(0d, Math.min(1d, value));
    }

    public static Builder builder(String id, String name, EntityType entityType) {
        return new Builder(id, name, entityType);
    }

    public static final class Builder {

        private final String id;
        private final String name;
        private final EntityType entityType;
        private final EnumMap<StatType, Double> stats = new EnumMap<>(StatType.class);
        private MobEquipment equipment;
        private MobLootTable lootTable;
        private MobRiderConfig riderConfig;
        private final Set<MobTag> tags = EnumSet.noneOf(MobTag.class);
        private final List<MobBehaviourFactory> behaviourFactories = new ArrayList<>();
        private Component displayName;
        private MobAiFactory aiFactory;
        private MobEntityFactory entityFactory;
        private double lootContributionThreshold = 0.1d;

        private Builder(String id, String name, EntityType entityType) {
            this.name = Objects.requireNonNull(name, "name");
            this.id = id;
            this.entityType = entityType;
        }

        public Builder stat(StatType statType, double value) {
            stats.put(statType, value);
            return this;
        }

        public Builder stats(Map<StatType, Double> values) {
            stats.putAll(values);
            return this;
        }

        public Builder equipment(MobEquipment equipment) {
            this.equipment = equipment;
            return this;
        }

        public Builder lootTable(MobLootTable lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        public Builder rider(MobRiderConfig riderConfig) {
            this.riderConfig = riderConfig;
            return this;
        }

        public Builder tag(MobTag tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder tags(Set<MobTag> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder behaviourFactory(@NotNull MobBehaviourFactory factory) {
            this.behaviourFactories.add(factory);
            return this;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder aiFactory(MobAiFactory aiFactory) {
            this.aiFactory = aiFactory;
            return this;
        }

        public Builder entityFactory(MobEntityFactory factory) {
            this.entityFactory = factory;
            return this;
        }

        public Builder lootContributionThreshold(double threshold) {
            this.lootContributionThreshold = threshold;
            return this;
        }

        public MobArchetype build() {
            if (entityFactory == null) {
                throw new IllegalStateException("entityFactory is required");
            }
            return new MobArchetype(this);
        }
    }
}

package org.example.mmo.npc.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroup;
import org.example.mmo.npc.mob.behaviour.MobBehaviourFactory;
import org.example.mmo.npc.mob.loot.MobLootTable;
import org.example.mmo.item.datas.StatType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable definition of a mob archetype (stats, equipment, behaviours, loot).
 */
public final class MobArchetype {

    private final String id;
    private final EntityType entityType;
    private final Map<StatType, Double> stats;
    private final MobEquipment equipment;
    private final MobLootTable lootTable;
    private final Optional<MobRiderConfig> riderConfig;
    private final Set<MobTag> tags;
    private final List<MobBehaviourFactory> behaviourFactories;
    private final EntityAIGroup baseAiGroup;
    private final MobEntityFactory entityFactory;

    private MobArchetype(Builder builder) {
        this.id = builder.id;
        this.entityType = builder.entityType;
        this.stats = Collections.unmodifiableMap(new EnumMap<>(builder.stats));
        this.equipment = builder.equipment != null ? builder.equipment : MobEquipment.EMPTY;
        this.lootTable = builder.lootTable != null ? builder.lootTable : MobLootTable.EMPTY;
        this.riderConfig = Optional.ofNullable(builder.riderConfig);
        this.tags = Set.copyOf(builder.tags);
        this.behaviourFactories = List.copyOf(builder.behaviourFactories);
        this.baseAiGroup = builder.baseAiGroup;
        this.entityFactory = builder.entityFactory;
    }

    public String id() {
        return id;
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

    public Optional<EntityAIGroup> baseAiGroup() {
        return Optional.ofNullable(baseAiGroup);
    }

    public MobEntityFactory entityFactory() {
        return entityFactory;
    }

    public static Builder builder(String id, EntityType entityType) {
        return new Builder(id, entityType);
    }

    public static final class Builder {

        private final String id;
        private final EntityType entityType;
        private final EnumMap<StatType, Double> stats = new EnumMap<>(StatType.class);
        private MobEquipment equipment;
        private MobLootTable lootTable;
        private MobRiderConfig riderConfig;
        private final Set<MobTag> tags = EnumSet.noneOf(MobTag.class);
        private final List<MobBehaviourFactory> behaviourFactories = new ArrayList<>();
        private EntityAIGroup baseAiGroup;
        private MobEntityFactory entityFactory;

        private Builder(String id, EntityType entityType) {
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

        public Builder baseAiGroup(EntityAIGroup baseAiGroup) {
            this.baseAiGroup = baseAiGroup;
            return this;
        }

        public Builder entityFactory(MobEntityFactory factory) {
            this.entityFactory = factory;
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

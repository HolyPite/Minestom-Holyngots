package org.example.mmo.npc.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.example.mmo.npc.mob.behaviour.MobBehaviour;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runtime representation of a spawned mob archetype.
 */
public final class MobInstance {

    private final MobArchetype archetype;
    private final Instance instance;
    private final UUID entityUuid;
    private final List<MobBehaviour> behaviours;

    public MobInstance(MobArchetype archetype,
                       Instance instance,
                       UUID entityUuid,
                       List<MobBehaviour> behaviours) {
        this.archetype = archetype;
        this.instance = instance;
        this.entityUuid = entityUuid;
        this.behaviours = List.copyOf(behaviours);
    }

    public MobArchetype archetype() {
        return archetype;
    }

    public Instance instance() {
        return instance;
    }

    public UUID entityUuid() {
        return entityUuid;
    }

    public List<MobBehaviour> behaviours() {
        return behaviours;
    }

    public Optional<LivingEntity> resolveEntity() {
        Entity entity = instance.getEntityByUuid(entityUuid);
        if (entity instanceof LivingEntity living) {
            return Optional.of(living);
        }
        return Optional.empty();
    }
}

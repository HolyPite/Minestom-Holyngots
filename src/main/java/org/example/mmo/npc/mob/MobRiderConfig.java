package org.example.mmo.npc.mob;

import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Optional rider configuration for mounted archetypes.
 *
 * @param entityType the Minestom entity type used for the rider
 * @param archetypeId optional reference to another MobArchetype to instantiate as the rider
 */
public record MobRiderConfig(@NotNull EntityType entityType,
                             Optional<String> archetypeId) {

    public MobRiderConfig(EntityType entityType, String archetypeId) {
        this(entityType, Optional.ofNullable(archetypeId));
    }
}

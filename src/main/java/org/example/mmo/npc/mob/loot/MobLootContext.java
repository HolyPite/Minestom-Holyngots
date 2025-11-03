package org.example.mmo.npc.mob.loot;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.example.mmo.npc.mob.MobInstance;

import java.util.Optional;

/**
 * Context received during loot generation.
 *
 * @param mobInstance the mob instance that died
 * @param killer optional entity that killed the mob (if available)
 * @param looter optional player designated to receive the drops (if available)
 */
public record MobLootContext(MobInstance mobInstance,
                             Optional<Entity> killer,
                             Optional<Player> looter,
                             Optional<LivingEntity> lastDamager) {

    public static MobLootContext of(MobInstance mobInstance,
                                    Entity killer,
                                    Player looter,
                                    LivingEntity lastDamager) {
        return new MobLootContext(
                mobInstance,
                Optional.ofNullable(killer),
                Optional.ofNullable(looter),
                Optional.ofNullable(lastDamager)
        );
    }
}

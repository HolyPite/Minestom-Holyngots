package org.example.mmo.npc.mob.loot;

import java.util.Optional;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.example.mmo.npc.mob.MobInstance;
import org.example.mmo.npc.mob.MobArchetype;

/**
 * Context received during loot generation.
 *
 * @param archetype the archetype associated with the loot
 * @param mobInstance optional mob instance that died (if loot rolled on death)
 * @param killer optional entity that killed the mob (if available)
 * @param looter optional player designated to receive the drops (if available)
 */
public record MobLootContext(MobArchetype archetype,
                             Optional<MobInstance> mobInstance,
                             Optional<Entity> killer,
                             Optional<Player> looter,
                             Optional<LivingEntity> lastDamager) {

    public static MobLootContext forMobDeath(MobInstance mobInstance,
                                             Entity killer,
                                             Player looter,
                                             LivingEntity lastDamager) {
        return new MobLootContext(
                mobInstance.archetype(),
                Optional.of(mobInstance),
                Optional.ofNullable(killer),
                Optional.ofNullable(looter),
                Optional.ofNullable(lastDamager)
        );
    }

    public static MobLootContext forBundle(MobArchetype archetype, Player opener) {
        return new MobLootContext(
                archetype,
                Optional.empty(),
                Optional.of(opener),
                Optional.ofNullable(opener),
                Optional.empty()
        );
    }
}

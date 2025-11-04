package org.example.mmo.mob.archetype.archetypes;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobRegistry;
import org.example.mmo.mob.MobTag;
import org.example.mmo.mob.ai.ais.ProjectileShooterAiFactory;
import org.example.mmo.mob.loot.loots.BanditArcherLoot;

public final class CultPyromancerMob {

    public static final String ID = "cult_pyromancer";
    public static final String NAME = "Pyromancien du culte";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private CultPyromancerMob() {
    }

    private static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.EVOKER)
                .entityFactory(() -> new EntityCreature(EntityType.EVOKER))
                .displayName(Component.text(NAME))
                .stat(StatType.HEALTH, 32)
                .stat(StatType.ATTACK, 24)
                .tag(MobTag.RANGED)
                .aiFactory(new ProjectileShooterAiFactory(
                        50,
                        28,
                        16,
                        1.35,
                        0.02,
                        false,
                        EntityType.SMALL_FIREBALL
                ))
                .lootTable(BanditArcherLoot.TABLE)
                .build();
    }
}

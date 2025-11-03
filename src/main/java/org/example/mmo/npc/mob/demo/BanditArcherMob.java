package org.example.mmo.npc.mob.demo;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.MobAiFactories;
import org.example.mmo.npc.mob.loot.MobLootTable;

public final class BanditArcherMob {

    public static final String ID = "bandit_archer";
    public static final String NAME = "Bandit archer";

    private BanditArcherMob() {
    }

    public static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.SKELETON)
                .entityFactory(() -> new EntityCreature(EntityType.SKELETON))
                .aiFactory(MobAiFactories.passiveSentry())
                .stat(StatType.HEALTH, 26)
                .tag(MobTag.RANGED)
                .lootTable(MobLootTable.EMPTY)
                .displayName(Component.text(NAME))
                .build();
    }

    public static void register() {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(create());
        }
    }
}




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

public final class BanditSkirmisherMob {

    public static final String ID = "bandit_skirmisher";
    public static final String NAME = "Bandit maraudeur";

    private BanditSkirmisherMob() {
    }

    public static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.ZOMBIE)
                .entityFactory(() -> new EntityCreature(EntityType.ZOMBIE))
                .aiFactory(MobAiFactories.meleeCharger(1.2, 25))
                .stat(StatType.HEALTH, 32)
                .tag(MobTag.AGGRESSIVE)
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




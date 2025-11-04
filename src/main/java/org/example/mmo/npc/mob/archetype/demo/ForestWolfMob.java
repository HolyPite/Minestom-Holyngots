package org.example.mmo.npc.mob.archetype.demo;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.MobAiFactories;
import org.example.mmo.npc.mob.behaviour.impl.AttributeSetterBehaviour;
import org.example.mmo.npc.mob.behaviour.impl.EnrageOnLowHealthBehaviour;
import org.example.mmo.npc.mob.loot.MobLootTable;

public final class ForestWolfMob {

    public static final String ID = "forest_wolf";
    public static final String NAME = "Loup des bois";

    private ForestWolfMob() {
    }

    public static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.WOLF)
                .entityFactory(() -> new EntityCreature(EntityType.WOLF))
                .aiFactory(MobAiFactories.meleeCharger(1.0, 25))
                .stat(StatType.HEALTH, 24)
                .tag(MobTag.AGGRESSIVE)
                .behaviourFactory((archetype, entity) -> new AttributeSetterBehaviour(entity, Attribute.MOVEMENT_SPEED, 0.30))
                .behaviourFactory((archetype, entity) -> new EnrageOnLowHealthBehaviour(entity, 0.35f))
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

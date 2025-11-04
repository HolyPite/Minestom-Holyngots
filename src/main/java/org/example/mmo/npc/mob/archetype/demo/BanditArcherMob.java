package org.example.mmo.npc.mob.archetype.demo;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.MobAiFactories;
import org.example.mmo.npc.mob.behaviour.impl.EquipItemBehaviour;
import org.example.mmo.npc.mob.loot.MobLootTable;

public final class BanditArcherMob {

    public static final String ID = "bandit_archer";
    public static final String NAME = "Bandit archer";

    private BanditArcherMob() {
    }

    public static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.SKELETON)
                .entityFactory(() -> new EntityCreature(EntityType.SKELETON))
                .aiFactory(MobAiFactories.archer(30, 30, 12, 1.6, 0.12))
                .stat(StatType.HEALTH, 26)
                .tag(MobTag.RANGED)
                .behaviourFactory((archetype, entity) -> new EquipItemBehaviour(entity, EquipmentSlot.MAIN_HAND, ItemStack.of(Material.BOW)))
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


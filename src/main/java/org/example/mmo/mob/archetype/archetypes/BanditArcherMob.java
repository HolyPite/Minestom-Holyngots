package org.example.mmo.mob.archetype.archetypes;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobRegistry;
import org.example.mmo.mob.MobTag;
import org.example.mmo.mob.ai.ais.ArcherAiFactory;
import org.example.mmo.mob.behaviour.behaviours.EquipItemBehaviour;
import org.example.mmo.mob.loot.loots.BanditArcherLoot;

public final class BanditArcherMob {

    public static final String ID = "bandit_archer";
    public static final String NAME = "Bandit archer";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private BanditArcherMob() {
    }

    private static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.SKELETON)
                .entityFactory(() -> new EntityCreature(EntityType.SKELETON))
                .aiFactory(new ArcherAiFactory(55, 28, 14, 0.5, 0.025))
                .stat(StatType.HEALTH, 26)
                .tag(MobTag.RANGED)
                .behaviourFactory((archetype, entity) -> new EquipItemBehaviour(entity, EquipmentSlot.MAIN_HAND, ItemStack.of(Material.BOW)))
                .lootTable(BanditArcherLoot.TABLE)
                .displayName(Component.text(NAME))
                .build();
    }
}

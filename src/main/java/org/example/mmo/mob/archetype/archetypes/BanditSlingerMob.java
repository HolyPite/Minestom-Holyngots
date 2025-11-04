package org.example.mmo.mob.archetype.archetypes;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobEquipment;
import org.example.mmo.mob.MobRegistry;
import org.example.mmo.mob.MobTag;
import org.example.mmo.mob.ai.ais.ProjectileShooterAiFactory;
import org.example.mmo.mob.loot.loots.BanditSkirmisherLoot;

public final class BanditSlingerMob {

    public static final String ID = "bandit_slinger";
    public static final String NAME = "Bandit frondeur";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private BanditSlingerMob() {
    }

    private static MobArchetype create() {
        MobEquipment equipment = MobEquipment.builder()
                .equip(EquipmentSlot.MAIN_HAND, "bandit_coin")
                .build();

        return MobArchetype.builder(ID, NAME, EntityType.PILLAGER)
                .entityFactory(() -> new EntityCreature(EntityType.PILLAGER))
                .displayName(Component.text(NAME))
                .stat(StatType.HEALTH, 24)
                .tag(MobTag.RANGED)
                .equipment(equipment)
                .aiFactory(new ProjectileShooterAiFactory(
                        25,
                        22,
                        12,
                        0.9,
                        0.12,
                        true,
                        EntityType.EGG
                ))
                .lootTable(BanditSkirmisherLoot.TABLE)
                .build();
    }
}

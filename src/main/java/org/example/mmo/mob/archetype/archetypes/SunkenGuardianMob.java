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
import org.example.mmo.mob.ai.ais.SunkenGuardianAiFactory;
import org.example.mmo.mob.behaviour.behaviours.BulwarkShieldBehaviour;
import org.example.mmo.mob.behaviour.behaviours.TidalRageBehaviour;
import org.example.mmo.mob.loot.loots.SunkenGuardianLoot;
import org.example.mmo.mob.loot.loots.SunkenGuardianQuestLoot;

public final class SunkenGuardianMob {

    public static final String ID = "sunken_guardian";
    public static final String NAME = "Gardien des ruines";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private SunkenGuardianMob() {
    }

    private static MobArchetype create() {
        MobEquipment equipment = MobEquipment.builder()
                .equip(EquipmentSlot.MAIN_HAND, "sunken_guardian_halberd")
                .equip(EquipmentSlot.CHESTPLATE, "sunken_guardian_plate")
                .build();

        return MobArchetype.builder(ID, NAME, EntityType.DROWNED)
                .entityFactory(() -> new EntityCreature(EntityType.DROWNED))
                .displayName(Component.text(NAME))
                .stat(StatType.HEALTH, 60)
                .stat(StatType.ARMOR, 18)
                .stat(StatType.ATTACK, 18)
                .lootContributionThreshold(0.12d)
                .equipment(equipment)
                .tag(MobTag.AGGRESSIVE)
                .aiFactory(new SunkenGuardianAiFactory(1.0, 30, 28f))
                .behaviourFactory((archetype, entity) -> new BulwarkShieldBehaviour(entity, 1))
                .behaviourFactory((archetype, entity) -> new TidalRageBehaviour(entity, 1, 1))
                .lootTable(SunkenGuardianLoot.TABLE)
                .questLootTable(SunkenGuardianQuestLoot.TABLE)
                .build();
    }
}

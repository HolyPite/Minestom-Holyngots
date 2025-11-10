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
import org.example.mmo.mob.loot.loots.SunkenGuardianLoot;
import org.example.mmo.mob.loot.loots.SunkenGuardianQuestLoot;
import org.example.mmo.mob.skill.MobSkills;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

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
                .skillBehaviour(MobSkills.bulwark(1, 100, Duration.ofSeconds(8), SkillTrigger.ENTITY_TICK))
                .skillBehaviour(MobSkills.frenzy(0.5, 80, 1, 1, Duration.ofSeconds(10), SkillTrigger.ENTITY_DAMAGED))
                .lootTable(SunkenGuardianLoot.TABLE)
                .questLootTable(SunkenGuardianQuestLoot.TABLE)
                .build();
    }
}

package org.example.mmo.mob.archetype.archetypes;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.item.skill.SkillTrigger;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobEquipment;
import org.example.mmo.mob.MobRegistry;
import org.example.mmo.mob.MobTag;
import org.example.mmo.mob.ai.ais.ProjectileShooterAiFactory;
import org.example.mmo.mob.loot.loots.SunkenGuardianLoot;
import org.example.mmo.mob.skill.MobSkills;
import java.time.Duration;

public final class TidalTridentMob {

    public static final String ID = "tidal_trident";
    public static final String NAME = "Trident des marees";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private TidalTridentMob() {
    }

    private static MobArchetype create() {
        MobEquipment equipment = MobEquipment.builder()
                .equip(EquipmentSlot.MAIN_HAND, "sunken_guardian_halberd")
                .build();

        return MobArchetype.builder(ID, NAME, EntityType.DROWNED)
                .entityFactory(() -> new EntityCreature(EntityType.DROWNED))
                .displayName(Component.text(NAME))
                .stat(StatType.HEALTH, 50)
                .stat(StatType.ATTACK, 20)
                .tag(MobTag.RANGED)
                .equipment(equipment)
                .aiFactory(new ProjectileShooterAiFactory(
                        40,
                        30,
                        18,
                        1.4,
                        0.04,
                        true,
                        EntityType.TRIDENT
                ))
                .skillBehaviour(MobSkills.dash(10.0, 0.15, Duration.ofSeconds(6), SkillTrigger.ENTITY_AGGRO))
                .skillBehaviour(MobSkills.teleport(6.0, 0.2, Duration.ofSeconds(12), SkillTrigger.ENTITY_DAMAGED))
                .lootTable(SunkenGuardianLoot.TABLE)
                .build();
    }
}

package org.example.mmo.npc.mob.archetype.archetypes;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.npc.mob.MobArchetype;
import org.example.mmo.npc.mob.MobRegistry;
import org.example.mmo.npc.mob.MobTag;
import org.example.mmo.npc.mob.ai.ais.MeleeChargerAiFactory;
import org.example.mmo.npc.mob.behaviour.behaviours.AttributeSetterBehaviour;
import org.example.mmo.npc.mob.loot.loots.BanditSkirmisherLoot;

public final class BanditSkirmisherMob {

    public static final String ID = "bandit_skirmisher";
    public static final String NAME = "Bandit maraudeur";
    public static final MobArchetype ARCHETYPE = create();

    static {
        if (!MobRegistry.contains(ID)) {
            MobRegistry.register(ARCHETYPE);
        }
    }

    private BanditSkirmisherMob() {
    }

    private static MobArchetype create() {
        return MobArchetype.builder(ID, NAME, EntityType.ZOMBIE)
                .entityFactory(() -> new EntityCreature(EntityType.ZOMBIE))
                .aiFactory(new MeleeChargerAiFactory(0.9, 30))
                .stat(StatType.HEALTH, 32)
                .tag(MobTag.AGGRESSIVE)
                .behaviourFactory((archetype, entity) -> new AttributeSetterBehaviour(entity, Attribute.MOVEMENT_SPEED, 0.25))
                .lootTable(BanditSkirmisherLoot.TABLE)
                .displayName(Component.text(NAME))
                .build();
    }
}

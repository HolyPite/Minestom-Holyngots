package org.example.mmo.npc.mob;

import net.minestom.server.entity.LivingEntity;

@FunctionalInterface
public interface MobEntityFactory {

    LivingEntity create();
}

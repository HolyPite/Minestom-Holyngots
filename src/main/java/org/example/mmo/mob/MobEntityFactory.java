package org.example.mmo.mob;

import net.minestom.server.entity.LivingEntity;

@FunctionalInterface
public interface MobEntityFactory {

    LivingEntity create();
}

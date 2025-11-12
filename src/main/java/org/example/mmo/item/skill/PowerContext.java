package org.example.mmo.item.skill;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Context passed to powers when activated.
 */
public record PowerContext(@NotNull LivingEntity entity,
                           @NotNull ItemStack itemStack,
                           @NotNull SkillTrigger trigger,
                           @NotNull SkillTriggerData triggerData,
                           int level) {

    public Player asPlayer() {
        return entity instanceof Player player ? player : null;
    }
}

package org.example.mmo.item.skill;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Context passed to powers when activated.
 */
public record PowerContext(@NotNull Player player,
                           @NotNull ItemStack itemStack,
                           @NotNull SkillTrigger trigger,
                           @NotNull SkillTriggerData triggerData,
                           int level) {
}

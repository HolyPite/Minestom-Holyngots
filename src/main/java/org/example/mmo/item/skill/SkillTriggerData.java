package org.example.mmo.item.skill;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public sealed interface SkillTriggerData permits SkillTriggerData.InventoryClickData,
        SkillTriggerData.InventoryChangeData,
        SkillTriggerData.BlockTargetData,
        SkillTriggerData.EntityTargetData,
        SkillTriggerData.HeldTickData,
        SkillTriggerData.SimpleData {

    record SimpleData() implements SkillTriggerData {}

    record InventoryClickData(InventoryPreClickEvent event) implements SkillTriggerData {}

    record InventoryChangeData(InventoryItemChangeEvent event, @Nullable ItemStack previous, @Nullable ItemStack next)
            implements SkillTriggerData {}

    record BlockTargetData(Point position) implements SkillTriggerData {}

    record EntityTargetData(@Nullable Entity target) implements SkillTriggerData {}

    record HeldTickData(long aliveTicks) implements SkillTriggerData {}
}

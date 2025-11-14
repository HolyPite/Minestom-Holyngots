package org.example.mmo.dev.advancementui.model;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.Material;

public record SkillTreeDisplayPrototype(
        Component title,
        Component description,
        Material icon,
        FrameType frameType,
        float x,
        float y,
        String background
) {
}

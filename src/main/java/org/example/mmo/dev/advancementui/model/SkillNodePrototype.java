package org.example.mmo.dev.advancementui.model;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.Material;

import java.util.Map;

public record SkillNodePrototype(
        String id,
        Component title,
        Component description,
        Material icon,
        FrameType frameType,
        float x,
        float y,
        boolean toast,
        boolean hidden,
        String parentId,
        Map<String, Object> metadata
) {
}

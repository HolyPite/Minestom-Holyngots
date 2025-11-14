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
        boolean secret,
        String parentId,
        Map<String, Object> metadata
) {

    public SkillNodePrototype withPosition(float x, float y) {
        return new SkillNodePrototype(
                id,
                title,
                description,
                icon,
                frameType,
                x,
                y,
                toast,
                hidden,
                secret,
                parentId,
                metadata
        );
    }
}

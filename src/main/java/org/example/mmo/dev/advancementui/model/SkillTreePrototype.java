package org.example.mmo.dev.advancementui.model;

import org.example.mmo.dev.advancementui.definition.SkillTreeLayoutConstraints;

import java.util.Map;

public record SkillTreePrototype(
        String id,
        SkillTreeDisplayPrototype display,
        Map<String, SkillNodePrototype> nodes,
        SkillTreeLayoutConstraints layoutConstraints
) {
}

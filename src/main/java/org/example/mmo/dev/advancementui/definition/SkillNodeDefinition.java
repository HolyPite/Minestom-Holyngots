package org.example.mmo.dev.advancementui.definition;

import java.util.List;
import java.util.Map;

/**
 * Décrit un nœud (skill) de l'arbre.
 */
public record SkillNodeDefinition(
        String id,
        String title,
        String description,
        String icon,
        String frameType,
        float x,
        float y,
        boolean toast,
        boolean hidden,
        boolean secret,
        String parentId,
        Map<String, Object> metadata,
        List<SkillNodeDefinition> children
) {

    public SkillNodeDefinition {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

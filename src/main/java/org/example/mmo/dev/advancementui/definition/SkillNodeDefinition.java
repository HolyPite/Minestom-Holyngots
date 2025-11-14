package org.example.mmo.dev.advancementui.definition;

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
        String parentId,
        Map<String, Object> metadata
) {
}

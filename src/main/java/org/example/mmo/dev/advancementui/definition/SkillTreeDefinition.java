package org.example.mmo.dev.advancementui.definition;

import java.util.List;
import java.util.Objects;

/**
 * Définition sérialisable d'un arbre de compétences affiché via l'UI d'advancements.
 */
public record SkillTreeDefinition(
        String id,
        SkillTreeDisplayDefinition display,
        List<SkillNodeDefinition> nodes,
        SkillTreeLayoutConstraints layout
) {

    public SkillTreeDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(display, "display");
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }
}

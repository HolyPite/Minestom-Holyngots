package org.example.mmo.dev.advancementui.definition;

/**
 * Contraintes optionnelles pour valider la mise en page.
 */
public record SkillTreeLayoutConstraints(
        Float minX,
        Float maxX,
        Float minY,
        Float maxY,
        Float minDistance,
        Boolean autoArrange,
        Float horizontalStep,
        Float verticalSpacing
) {
}

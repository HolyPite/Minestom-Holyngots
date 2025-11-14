package org.example.mmo.dev.advancementui.definition;

/**
 * Paramètres visuels du root {@link net.minestom.server.advancements.AdvancementRoot}.
 */
public record SkillTreeDisplayDefinition(
        String title,
        String description,
        String icon,
        String frameType,
        float x,
        float y,
        String background
) {
}

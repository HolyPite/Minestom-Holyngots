package org.example.mmo.npc;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;

import java.util.List;

/**
 * Represents the static definition of a Non-Player Character (NPC).
 */
public record NPC(
        String id,
        Component name,
        EntityType entityType,
        Pos spawnPosition,
        List<Component> randomDialogues,
        Sound soundEffect
) {

    /**
     * Overloaded constructor for humanoid NPCs with a default sound.
     */
    public NPC(String id, Component name, Pos spawnPosition, List<Component> randomDialogues) {
        this(id, name, EntityType.VILLAGER, spawnPosition, randomDialogues, Sound.sound(Key.key("minecraft:entity.villager.ambient"), Sound.Source.NEUTRAL, 1f, 1f));
    }

    /**
     * Overloaded constructor for humanoid NPCs with a custom sound.
     */
    public NPC(String id, Component name, Pos spawnPosition, List<Component> randomDialogues, Sound sound) {
        this(id, name, EntityType.VILLAGER, spawnPosition, randomDialogues, sound);
    }
}

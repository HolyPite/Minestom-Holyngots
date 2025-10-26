package org.example.mmo.npc;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;

import java.util.List;

/**
 * Represents the static definition of a Non-Player Character (NPC).
 */
public record NPC(
        String id,
        Component name,
        Pos spawnPosition,
        List<Component> randomDialogues,
        Sound soundEffect
) {

    /**
     * Overloaded constructor to provide a default sound effect.
     */
    public NPC(String id, Component name, Pos spawnPosition, List<Component> randomDialogues) {
        this(id, name, spawnPosition, randomDialogues, Sound.sound(Key.key("minecraft:entity.villager.ambient"), Sound.Source.NEUTRAL, 10.0f, 1f));
    }
}

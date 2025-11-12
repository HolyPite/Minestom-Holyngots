package org.example.mmo.quest.structure;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.List;

/**
 * Defines a quest, composed of multiple steps and global properties.
 */
public class Quest {
    public final String id;
    public final Component name;
    public final List<QuestStep> steps;
    public final Component description;

    // New properties for more modularity
    public final boolean repeatable;
    public final Duration cooldown;

    public Quest(String id, Component name, Component description, List<QuestStep> steps, boolean repeatable, Duration cooldown) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = List.copyOf(steps);
        this.repeatable = repeatable;
        this.cooldown = cooldown;
    }

    // Overloaded constructor for simpler quests
    public Quest(String id, Component name, Component description, List<QuestStep> steps) {
        this(id, name, description, steps, false, Duration.ZERO);
    }
}

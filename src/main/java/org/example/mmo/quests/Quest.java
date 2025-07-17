package org.example.mmo.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.items.GameItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Définition d'une quête composée de plusieurs étapes.
 */
public class Quest {
    public final String id;
    public final Component name;
    public final List<QuestStep> steps;
    public final List<GameItem> rewards;
    public final Component description;

    public Quest(String id, Component name, Component description,
                 List<QuestStep> steps, List<GameItem> rewards) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = List.copyOf(steps);
        this.rewards = List.copyOf(rewards);
    }

    public Quest(String id, Component name, Component description) {
        this(id, name, description, new ArrayList<>(), new ArrayList<>());
    }
}

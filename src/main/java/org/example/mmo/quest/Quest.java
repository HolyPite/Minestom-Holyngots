package org.example.mmo.quest;

import net.kyori.adventure.text.Component;
import org.example.mmo.item.GameItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Définition d'une quête composée de plusieurs étapes.
 */
public class Quest {
    public final String id;
    public final Component name;
    public final List<QuestStep> steps;
    public final Component description;

    public Quest(String id, Component name, Component description,
                 List<QuestStep> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = List.copyOf(steps);
    }

    public Quest(String id, Component name, Component description) {
        this(id, name, description, new ArrayList<>());
    }
}

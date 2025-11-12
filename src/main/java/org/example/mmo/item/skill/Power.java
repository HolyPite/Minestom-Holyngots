package org.example.mmo.item.skill;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface describing a reusable power (dash, explosion, etc.).
 */
@FunctionalInterface
public interface Power {

    void execute(@NotNull PowerContext context, @NotNull PowerParameters parameters);
}

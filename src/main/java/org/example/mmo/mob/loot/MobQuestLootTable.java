package org.example.mmo.mob.loot;

import java.util.Collections;
import java.util.List;

/**
 * Stores quest-specific drops for an archetype.
 */
public final class MobQuestLootTable {

    public static final MobQuestLootTable EMPTY = new MobQuestLootTable(List.of());

    private final List<MobQuestLootEntry> entries;

    public MobQuestLootTable(List<MobQuestLootEntry> entries) {
        this.entries = Collections.unmodifiableList(List.copyOf(entries));
    }

    public List<MobQuestLootEntry> entries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}

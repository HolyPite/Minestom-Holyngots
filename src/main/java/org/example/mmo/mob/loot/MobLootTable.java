package org.example.mmo.mob.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple weighted table storing individual loot entries for an archetype.
 */
public final class MobLootTable {

    public static final MobLootTable EMPTY = new MobLootTable(List.of());

    private final List<MobLootEntry> entries;

    public MobLootTable(List<MobLootEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public List<MobLootEntry> entries() {
        return entries;
    }

    /**
     * Rolls the current table using the supplied random instance.
     */
    public List<MobLootEntry> roll(Random random, MobLootContext context) {
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<MobLootEntry> results = new ArrayList<>();
        for (MobLootEntry entry : entries) {
            if (!entry.condition().test(context)) {
                continue;
            }
            if (random.nextDouble() <= entry.chance()) {
                results.add(entry);
            }
        }
        return results;
    }
}

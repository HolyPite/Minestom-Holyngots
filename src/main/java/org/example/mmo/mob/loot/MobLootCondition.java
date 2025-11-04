package org.example.mmo.mob.loot;

/**
 * Simple predicate hook to decide if a loot entry should roll for the given context.
 */
@FunctionalInterface
public interface MobLootCondition {

    boolean test(MobLootContext context);

    MobLootCondition ALWAYS_TRUE = context -> true;
}

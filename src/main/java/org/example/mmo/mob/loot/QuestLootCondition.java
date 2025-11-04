package org.example.mmo.mob.loot;

import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;

/**
 * Additional predicate evaluated when generating quest-specific loot for a player.
 */
@FunctionalInterface
public interface QuestLootCondition {

    QuestLootCondition ALWAYS_TRUE = (player, data) -> true;

    boolean test(Player player, PlayerData data);
}

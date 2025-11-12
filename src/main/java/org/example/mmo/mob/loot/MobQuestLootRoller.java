package org.example.mmo.mob.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves quest-dependent loot and converts it into ItemStacks.
 */
public final class MobQuestLootRoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobQuestLootRoller.class);

    private MobQuestLootRoller() {
    }

    public static List<ItemStack> generateLoot(MobQuestLootTable table,
                                               Player player,
                                               PlayerData data,
                                               Random random) {
        if (table == null || table.isEmpty()) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (MobQuestLootEntry entry : table.entries()) {
            if (!entry.condition().test(player, data)) {
                continue;
            }
            if (random.nextDouble() > entry.chance()) {
                continue;
            }
            GameItem gameItem = ItemRegistry.byId(entry.itemId());
            if (gameItem == null) {
                LOGGER.warn("Unknown quest loot item '{}' for player {}", entry.itemId(), player.getUsername());
                continue;
            }
            int amount = entry.minAmount();
            if (entry.maxAmount() > entry.minAmount()) {
                amount += random.nextInt(entry.maxAmount() - entry.minAmount() + 1);
            }
            drops.add(gameItem.toItemStack().withAmount(Math.max(1, Math.min(64, amount))));
        }
        return drops;
    }
}

package org.example.mmo.npc.mob.loot;

import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.npc.mob.MobArchetype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Converts loot table entries into concrete drops.
 */
public final class MobLootRoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobLootRoller.class);

    private MobLootRoller() {
    }

    public static List<ItemStack> generateLoot(MobLootContext context,
                                              Random random) {
        MobArchetype archetype = context.archetype();
        List<MobLootEntry> rolledEntries = archetype.lootTable().roll(random, context);
        List<ItemStack> drops = new ArrayList<>(rolledEntries.size());
        for (MobLootEntry entry : rolledEntries) {
            GameItem gameItem = ItemRegistry.byId(entry.itemId());
            if (gameItem == null) {
                LOGGER.warn("Unknown loot item '{}' for mob '{}'", entry.itemId(), archetype.id());
                continue;
            }
            int amount = entry.minAmount();
            if (entry.maxAmount() > entry.minAmount()) {
                amount += random.nextInt(entry.maxAmount() - entry.minAmount() + 1);
            }
            ItemStack stack = gameItem.toItemStack().withAmount(Math.max(1, Math.min(64, amount)));
            drops.add(stack);
        }
        return drops;
    }
}

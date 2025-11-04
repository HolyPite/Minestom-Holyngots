package org.example.mmo.mob.loot;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemDelivery;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.ItemUtils;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.mob.MobArchetype;
import org.example.mmo.mob.MobRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles registration and usage of mob loot bundles (capsules opened later).
 */
public final class MobLootBundles {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobLootBundles.class);
    private static final String BUNDLE_PREFIX = "mob_bundle:";

    private MobLootBundles() {
    }

    public static void ensureRegistered(MobArchetype archetype) {
        String bundleId = bundleIdFor(archetype.id());
        if (ItemRegistry.byId(bundleId) != null) {
            return;
        }
        GameItem bundleItem = new GameItem.Builder(bundleId,
                Component.text("Reste de " + archetype.name(), NamedTextColor.AQUA))
                .category(Category.MISC)
                .rarity(Rarity.UNCOMMON)
                .material(Material.BUNDLE)
                .tradable(false)
                .stackSize(16)
                .story("Ouvrez-le pour r\u00E9clamer les butins du mob.")
                .build();
        ItemRegistry.register(bundleItem);
        LOGGER.info("Registered loot bundle item {} for mob {}", bundleId, archetype.id());
    }

    public static ItemStack createBundleStack(MobArchetype archetype) {
        ensureRegistered(archetype);
        GameItem bundleItem = ItemRegistry.byId(bundleIdFor(archetype.id()));
        if (bundleItem == null) {
            LOGGER.warn("Missing bundle item for mob {}", archetype.id());
            return ItemStack.AIR;
        }
        return bundleItem.toItemStack();
    }

    public static boolean isBundle(ItemStack stack) {
        return resolveBundleId(stack).isPresent();
    }

    public static Optional<MobArchetype> resolveArchetype(ItemStack stack) {
        return resolveBundleId(stack).flatMap(id -> Optional.ofNullable(MobRegistry.get(id)));
    }

    public static boolean openBundle(Player player, ItemStack stack) {
        Optional<MobArchetype> archetypeOptional = resolveArchetype(stack);
        if (archetypeOptional.isEmpty()) {
            return false;
        }
        MobArchetype archetype = archetypeOptional.get();
        MobLootContext context = MobLootContext.forBundle(archetype, player);
        var drops = MobLootRoller.generateLoot(context, ThreadLocalRandom.current());
        Instance instance = player.getInstance();
        Pos position = player.getPosition();
        for (ItemStack drop : drops) {
            ItemDelivery.giveOrDrop(player, drop, instance, position);
        }
        return true;
    }

    private static Optional<String> resolveBundleId(ItemStack stack) {
        GameItem item = ItemUtils.resolve(stack);
        if (item == null) {
            return Optional.empty();
        }
        String id = item.id;
        if (!id.startsWith(BUNDLE_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(id.substring(BUNDLE_PREFIX.length()));
    }

    private static String bundleIdFor(String archetypeId) {
        return BUNDLE_PREFIX + archetypeId;
    }
}

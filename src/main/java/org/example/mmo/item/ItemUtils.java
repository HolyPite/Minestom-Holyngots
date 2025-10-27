package org.example.mmo.item;

import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

public final class ItemUtils {

    private ItemUtils() {}

    @Nullable
    public static GameItem resolve(ItemStack stack) {
        if (stack == null || stack.isAir()) return null;
        Integer customModelData = stack.getTag(Tag.Integer("CustomModelData"));
        if (customModelData == null) return null;
        return ItemRegistry.fromCustomModelData(customModelData);
    }

    @Nullable
    public static String getId(ItemStack stack) {
        GameItem gameItem = resolve(stack);
        return (gameItem != null) ? gameItem.getId() : null;
    }
}

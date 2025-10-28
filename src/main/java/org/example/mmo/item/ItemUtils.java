package org.example.mmo.item;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomModelData;
import net.minestom.server.component.DataComponents;
import org.jetbrains.annotations.Nullable;

public final class ItemUtils {

    private ItemUtils() {}

    /**
     * Resolves an ItemStack to a GameItem by reading its custom model data.
     * @param stack The ItemStack to resolve.
     * @return The corresponding GameItem, or null if it's not a custom item.
     */
    @Nullable
    public static GameItem resolve(ItemStack stack) {
        if (stack == null || stack.isAir()) return null;
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return null;

        // In your version, the ID is stored in the first string of the CustomModelData
        if (cmd.strings().isEmpty()) return null;
        String id = cmd.strings().get(0);
        return ItemRegistry.byId(id);
    }

    /**
     * Gets the unique ID of a GameItem from an ItemStack, if it exists.
     * @param stack The ItemStack to check.
     * @return The GameItem's ID, or null if it's not a custom item.
     */
    @Nullable
    public static String getId(ItemStack stack) {
        GameItem gameItem = resolve(stack);
        return (gameItem != null) ? gameItem.id : null;
    }
}

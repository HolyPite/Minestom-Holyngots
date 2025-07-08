package org.example.items;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomModelData;

public final class ItemUtils {
    /** Récupère le GameItem correspondant à l’ItemStack (ou null) */
    public static GameItem resolve(ItemStack stack) {
        if (stack.isAir()) return null;

        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null || cmd.strings().isEmpty()) return null;

        String id = cmd.strings().get(0);              // id stocké ici
        return ItemRegistry.byId(id);
    }
}

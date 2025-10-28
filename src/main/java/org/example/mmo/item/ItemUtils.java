package org.example.mmo.item;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomModelData;
import net.minestom.server.component.DataComponents;
import org.jetbrains.annotations.Nullable;

public final class ItemUtils {

    private ItemUtils() {}

    /** Récupère le GameItem correspondant à l’ItemStack (ou null) */
    public static GameItem resolve(ItemStack stack) {
        if (stack.isAir()) return null;

        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null || cmd.strings().isEmpty()) return null;

        String id = cmd.strings().getFirst();              // id stocké ici
        //System.out.println("id = " + id);
        return ItemRegistry.byId(id);
    }

    @Nullable
    public static String getId(ItemStack stack) {
        GameItem gameItem = resolve(stack);
        return (gameItem != null) ? gameItem.id : null;
    }
}

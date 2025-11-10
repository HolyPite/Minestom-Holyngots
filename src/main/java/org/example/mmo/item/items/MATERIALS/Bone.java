package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class Bone {
    public static final GameItem INSTANCE;

    static {
        INSTANCE = new GameItem.Builder("bone", Component.text("Os", NamedTextColor.WHITE))
                .rarity(Rarity.COMMON)
                .category(Category.MATERIAL)
                .material(Material.BONE)
                .questItem(true) // Mark as quest item
                .build();

        ItemRegistry.register(INSTANCE);
    }

    private Bone() {}
}

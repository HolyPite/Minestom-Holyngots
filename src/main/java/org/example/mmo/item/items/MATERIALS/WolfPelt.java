package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class WolfPelt {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("wolf_pelt",
                Component.text("Peau de loup", NamedTextColor.GRAY))
                .category(Category.MATERIAL)
                .rarity(Rarity.COMMON)
                .material(Material.LEATHER)
                .stackSize(64)
                .story("Une fourrure chaude, prise sur un loup des bois.")
                .build();
        ItemRegistry.register(ITEM);
    }

    private WolfPelt() {
    }
}

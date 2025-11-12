package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class WolfFang {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("wolf_fang",
                Component.text("Croc de loup", NamedTextColor.WHITE))
                .category(Category.MATERIAL)
                .rarity(Rarity.UNCOMMON)
                .material(Material.IRON_NUGGET)
                .stackSize(64)
                .story("Un croc acéré, témoignage d'un combat victorieux.")
                .build();
        ItemRegistry.register(ITEM);
    }

    private WolfFang() {
    }
}

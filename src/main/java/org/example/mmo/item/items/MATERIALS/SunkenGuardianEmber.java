package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class SunkenGuardianEmber {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("sunken_guardian_ember",
                Component.text("Braise engloutie", NamedTextColor.DARK_AQUA))
                .category(Category.MATERIAL)
                .rarity(Rarity.RARE)
                .material(Material.BLAZE_POWDER)
                .stackSize(16)
                .tradable(false)
                .questItem(true)
                .story("Une braise encore chaude extraite d'un gardien englouti.")
                .build();
        ItemRegistry.register(ITEM);
    }

    private SunkenGuardianEmber() {
    }
}

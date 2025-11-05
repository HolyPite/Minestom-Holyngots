package org.example.mmo.item.items.AMMO;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.AmmoType;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class SteelArrowBundle {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("steel_arrow_bundle",
                Component.text("Faisceau de Fleches d'Acier", NamedTextColor.GRAY))
                .category(Category.AMMO)
                .rarity(Rarity.COMMON)
                .material(Material.ARROW)
                .stackSize(64)
                .story("Des fleches bien equilibrees pour arcs et arbaletes.")
                .ammo(options -> options.type(AmmoType.ARROW))
                .build();

        ItemRegistry.register(ITEM);
    }

    private SteelArrowBundle() {
    }
}

package org.example.mmo.item.items.ARMOR;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;
import org.example.utils.TKit;
import org.example.mmo.item.*;

public final class Polstron {

    public static final GameItem polstron =
            new GameItem.Builder("polstron",
                    TKit.createGradientText("Polstron",
                            TextColor.color(0xFFF1),
                            TextColor.color(0xFF5A)))

                    .rarity(Rarity.COMMON)
                    .category(Category.ARMOR)
                    .material(Material.LEATHER_CHESTPLATE)
                    .stat(StatType.ARMOR, 6)
                    .stat(StatType.HEALTH, 5)
                    .tradable(false)
                    .stackSize(1)
                    .build();

    static { ItemRegistry.register(polstron); }
}

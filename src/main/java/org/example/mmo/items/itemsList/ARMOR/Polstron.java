package org.example.mmo.items.itemsList.ARMOR;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import org.example.mmo.items.datas.Category;
import org.example.mmo.items.datas.Rarity;
import org.example.mmo.items.datas.StatType;
import org.example.utils.TKit;
import org.example.mmo.items.*;

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

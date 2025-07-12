package org.example.mmo.items.itemsList.WEAPON;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import org.example.mmo.items.datas.Category;
import org.example.mmo.items.datas.Rarity;
import org.example.mmo.items.datas.StatType;
import org.example.utils.TKit;
import org.example.mmo.items.*;

public final class Excalibur {

    public static final GameItem excalibur =
        new GameItem.Builder("excalibur",
                TKit.createGradientText("Excalibur",
                        TextColor.color(0xFFE600),
                        TextColor.color(0xC500FF)))

                .rarity(Rarity.LEGENDARY)
                .category(Category.WEAPON)
                .material(Material.DIAMOND_SWORD)
                .stat(StatType.ATTACK, -2)
                .stat(StatType.ATTACK_SPEED, -50)
                .stat(StatType.KNOCKBACK, 100)
                .tradable(false)
                .story("Forgée dans une ère oubliée,","seule la main d’un héros peut la brandir.")
                .stackSize(1)
                .build();

    static { ItemRegistry.register(excalibur); }
}

package org.example.mmo.item.items.WEAPON;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;

public final class SunkenGuardianHalberd {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("sunken_guardian_halberd",
                Component.text("Hallebarde des ruines", NamedTextColor.DARK_AQUA))
                .rarity(Rarity.RARE)
                .category(Category.WEAPON)
                .material(Material.TRIDENT)
                .stat(StatType.ATTACK, 32)
                .stat(StatType.CRIT_CHANCE, 5)
                .stackSize(1)
                .tradable(false)
                .build();
        ItemRegistry.register(ITEM);
    }

    private SunkenGuardianHalberd() {
    }
}

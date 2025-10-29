package org.example.mmo.item.items.WEAPON;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;

public final class Excalibur {
    public static final GameItem INSTANCE;

    static {
        INSTANCE = new GameItem.Builder("excalibur",
                Component.text("Excalibur", NamedTextColor.GOLD))
                .rarity(Rarity.LEGENDARY)
                .category(Category.WEAPON)
                .material(Material.DIAMOND_SWORD)
                .stat(StatType.ATTACK, 100)
                .stat(StatType.KNOCKBACK, 10)
                .tradable(false)
                .story("The legendary sword of King Arthur.")
                .stackSize(1)
                .questItem(true) // Mark as quest item
                .build();

        ItemRegistry.register(INSTANCE);
    }

    private Excalibur() {}
}

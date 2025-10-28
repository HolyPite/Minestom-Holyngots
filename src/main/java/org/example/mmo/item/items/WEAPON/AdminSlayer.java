package org.example.mmo.item.items.WEAPON;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;

public final class AdminSlayer {

    public static final GameItem INSTANCE;

    static {
        INSTANCE = new GameItem.Builder("admin_slayer",
                Component.text("Admin Slayer", NamedTextColor.RED))
                .rarity(Rarity.LEGENDARY)
                .category(Category.WEAPON)
                .material(Material.NETHERITE_SWORD)
                .stat(StatType.ATTACK, 9999)
                .stat(StatType.KNOCKBACK, -100)
                .tradable(false)
                .story("For testing purposes only.")
                .stackSize(1)
                .build();

        ItemRegistry.register(INSTANCE);
    }

    private AdminSlayer() {}
}

package org.example.mmo.item.items.ARMOR;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;

public final class SunkenGuardianPlate {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("sunken_guardian_plate",
                Component.text("Plastron des ruines", NamedTextColor.BLUE))
                .rarity(Rarity.UNCOMMON)
                .category(Category.ARMOR)
                .material(Material.DIAMOND_CHESTPLATE)
                .stat(StatType.ARMOR, 18)
                .stat(StatType.HEALTH, 20)
                .stackSize(1)
                .tradable(false)
                .build();
        ItemRegistry.register(ITEM);
    }

    private SunkenGuardianPlate() {
    }
}

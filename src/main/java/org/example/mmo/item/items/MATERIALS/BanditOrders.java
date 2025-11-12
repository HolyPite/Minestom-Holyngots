package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class BanditOrders {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("bandit_orders",
                Component.text("Ordres chiffonnés", NamedTextColor.DARK_RED))
                .category(Category.MATERIAL)
                .rarity(Rarity.RARE)
                .material(Material.PAPER)
                .stackSize(16)
                .story("Des instructions griffonnées indiquant la prochaine cible des bandits.")
                .build();
        ItemRegistry.register(ITEM);
    }

    private BanditOrders() {
    }
}

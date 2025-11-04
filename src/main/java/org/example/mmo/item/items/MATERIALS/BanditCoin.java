package org.example.mmo.item.items.MATERIALS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class BanditCoin {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("bandit_coin",
                Component.text("Pièce de bandit", NamedTextColor.GOLD))
                .category(Category.MATERIAL)
                .rarity(Rarity.UNCOMMON)
                .material(Material.GOLD_NUGGET)
                .stackSize(64)
                .story("Une pièce frappée d'un sceau pirate. Utile pour les marchands douteux.")
                .build();
        ItemRegistry.register(ITEM);
    }

    private BanditCoin() {
    }
}

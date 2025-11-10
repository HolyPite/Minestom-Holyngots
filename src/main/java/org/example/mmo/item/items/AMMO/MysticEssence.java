package org.example.mmo.item.items.AMMO;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.AmmoType;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;

public final class MysticEssence {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("mystic_essence",
                Component.text("Essence Mystique", NamedTextColor.LIGHT_PURPLE))
                .category(Category.AMMO)
                .rarity(Rarity.UNCOMMON)
                .material(Material.AMETHYST_SHARD)
                .stackSize(64)
                .story("Concentre les residus d'energie arcanique.")
                .ammo(options -> options.type(AmmoType.MAGIC))
                .build();

        ItemRegistry.register(ITEM);
    }

    private MysticEssence() {
    }
}

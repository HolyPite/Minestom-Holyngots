// org.example.items.DEBUG.StatsGrimoire
package org.example.items.itemsList.DEV;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.items.datas.Category;
import org.example.items.datas.Rarity;
import org.example.items.datas.Stats;
import org.example.utils.TKit;
import org.example.items.*;
import net.minestom.server.entity.Player;

public final class StatsGrimoire {

    public static final GameItem ITEM =
            new GameItem.Builder("stats_grimoire",
                    TKit.createGradientText("Grimoire des Stats", NamedTextColor.GOLD, NamedTextColor.YELLOW))
                    .category(Category.MISC)
                    .rarity(Rarity.UNCOMMON)
                    .material(Material.BOOK)
                    .tradable(false)
                    .stackSize(1)
                    .story("Clic-droit (air ou bloc) pour","afficher vos attributs actuels.")
                    .build();

    static {
        ItemRegistry.register(ITEM);

        /* ---- register behaviour on the bus ---- */
        ItemEventsCustom.register(ITEM, new ItemEventsCustom.Behaviour() {

            @Override
            public void onInventoryClic(Player p, ItemStack stack, InventoryPreClickEvent e) {
                Stats.refresh(p);
                e.setCancelled(true);
            }

        });
    }

    private StatsGrimoire() {}
}

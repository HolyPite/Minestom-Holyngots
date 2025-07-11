// org.example.items.DEBUG.StatsGrimoire
package org.example.items.itemsList.DEV;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.utils.TKit;
import org.example.combats.*;
import org.example.items.*;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class StatsGrimoire {

    public static final GameItem ITEM =
            new GameItem.Builder("stats_grimoire",
                    TKit.createGradientText("Grimoire des Stats", NamedTextColor.GOLD, NamedTextColor.YELLOW))
                    .category(Category.MISC)
                    .rarity(Rarity.UNCOMMON)
                    .material(Material.WRITTEN_BOOK)
                    .tradable(false)
                    .stackSize(1)
                    .story("Clic-droit (air ou bloc) pour","afficher vos attributs actuels.")
                    .build();

    static {
        ItemRegistry.register(ITEM);

        /* ---- register behaviour on the bus ---- */
        CustomItemEvents.register(ITEM, new CustomItemEvents.Behaviour() {

            @Override
            public void onInventoryClic(Player p, ItemStack stack, InventoryPreClickEvent e) {
                updateStats(p,stack,e);
                e.setCancelled(true);
            }

            private void updateStats(Player player, ItemStack stack, InventoryPreClickEvent e) {

                double maxHp = HealthUtils.getCustomMax(player);
                double curHp = HealthUtils.getCustom(player);

                List<Component> lines = new ArrayList<>();
                lines.add(Component.text("=== Vos statistiques ===", NamedTextColor.AQUA));
                lines.add(Component.text("PV : " + (int) curHp + " / " + (int) maxHp));

                for (StatType st : StatType.values()) {
                    int v = CombatEngine.getTotal(player, st);
                    if (v == 0) continue;
                    String val = (st.kind == StatType.ValueKind.FLAT) ? "" + v : v + " %";
                    lines.add(Component.text("• " + st.label + " : " + val, NamedTextColor.GRAY));
                }

                ItemStack it = stack.with(DataComponents.LORE,lines);
                player.getInventory().setItemStack(e.getSlot(),it);
            }
        });
    }

    private StatsGrimoire() {}
}

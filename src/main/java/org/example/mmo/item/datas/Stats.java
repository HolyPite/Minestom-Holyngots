package org.example.mmo.item.datas;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.mmo.combat.util.StatUtils;
import org.example.mmo.item.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class Stats {

    public static void refresh(Player player) {

        int slot = 17;

        ItemStack grimoire = player.getInventory().getItemStack(slot);

        if (grimoire.material() != Material.WRITABLE_BOOK) {
            // This message was causing a crash if called during the configuration phase.
            // The logic now silently gives the book if it's missing.
            // player.sendMessage("WTF t'as pas le bouquin!");
            ItemStack  it = ItemRegistry.byId("stats_grimoire").toItemStack();
            player.getInventory().setItemStack(17,it);
            return;
        }

        // Use StatUtils to get max health and player.getHealth() for current health
        double maxHp = StatUtils.getTotal(player, StatType.HEALTH);
        double curHp = player.getHealth();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.text("=== Vos statistiques ===", NamedTextColor.AQUA));
        lines.add(Component.text("PV : " + (int) curHp + " / " + (int) maxHp));

        for (StatType st : StatType.values()) {
            // Use the new StatUtils class to get the total for each stat
            int v = StatUtils.getTotal(player, st);
            if (v == 0) continue;

            // Don't display HEALTH again as it's already shown
            if (st == StatType.HEALTH) continue;

            String val = (st.kind == StatType.ValueKind.FLAT) ? "" + v : v + " %";
            lines.add(Component.text("• " + st.label + " : " + val, NamedTextColor.GRAY));
        }

        ItemStack it = grimoire.with(DataComponents.LORE,lines);
        player.getInventory().setItemStack(slot,it);
    }
}

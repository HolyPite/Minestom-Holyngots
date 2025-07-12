package org.example.mmo.items.datas;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.example.mmo.combats.CombatEngine;
import org.example.mmo.combats.HealthUtils;

import java.util.ArrayList;
import java.util.List;

public class Stats {

    public static void refresh(Player player) {

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

        int slot = 17;

        ItemStack stack = player.getInventory().getItemStack(slot);
        ItemStack it = stack.with(DataComponents.LORE,lines);
        player.getInventory().setItemStack(slot,it);
        //lines.forEach(player::sendMessage);
    }
}

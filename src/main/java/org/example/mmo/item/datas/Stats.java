package org.example.mmo.item.datas;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.mmo.combat.CombatEngine;
import org.example.mmo.combat.HealthUtils;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class Stats {

    public static void refresh(Player player) {

        int slot = 17;

        ItemStack grimoire = player.getInventory().getItemStack(slot);

        if (grimoire.material() != Material.WRITABLE_BOOK) {
            player.sendMessage("WTF t'as pas le bouquin!");
            ItemStack  it = ItemRegistry.byId("stats_grimoire").toItemStack();
            player.getInventory().setItemStack(17,it);
            //player.getInventory().setItemStack(slot, );
            return;
        };

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


        ItemStack it = grimoire.with(DataComponents.LORE,lines);
        player.getInventory().setItemStack(slot,it);
        //lines.forEach(player::sendMessage);
    }
}

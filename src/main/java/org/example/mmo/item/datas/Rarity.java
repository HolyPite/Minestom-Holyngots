package org.example.mmo.item.datas;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rarity {
    USELESS(NamedTextColor.GRAY),
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.GREEN),
    RARE(NamedTextColor.BLUE),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(NamedTextColor.GOLD),
    MYTHIC(NamedTextColor.DARK_RED),
    UNIC(NamedTextColor.BLACK);

    public final NamedTextColor color;
    Rarity(NamedTextColor c){ this.color = c; }
}

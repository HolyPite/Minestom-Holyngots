package org.example.mmo.item.datas;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    USELESS("Useless", NamedTextColor.GRAY),
    COMMON("Common", NamedTextColor.WHITE),
    UNCOMMON("Uncommon", NamedTextColor.GREEN),
    RARE("Rare", NamedTextColor.BLUE),
    EPIC("Epic", NamedTextColor.DARK_PURPLE),
    LEGENDARY("Legendary", NamedTextColor.GOLD),
    MYTHIC("Mythic", NamedTextColor.DARK_RED),
    UNIC("Unic", NamedTextColor.BLACK);
    ;

    private final String name;
    private final TextColor color;

    Rarity(String name, TextColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public TextColor color() {
        return color;
    }

    public Component toComponent() {
        return Component.text(name, color);
    }
}

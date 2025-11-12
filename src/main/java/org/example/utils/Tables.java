package org.example.utils;

import net.minestom.server.color.DyeColor;
import net.minestom.server.item.Material;

import java.util.EnumMap;
import java.util.Map;

/** Conversions pratiques entre couleurs et matériaux. */
public final class Tables {

    public static final Map<DyeColor, Material> WOOL_BY_COLOR = new EnumMap<>(DyeColor.class);

    static {
        WOOL_BY_COLOR.put(DyeColor.WHITE,       Material.WHITE_WOOL);
        WOOL_BY_COLOR.put(DyeColor.ORANGE,      Material.ORANGE_WOOL);
        WOOL_BY_COLOR.put(DyeColor.MAGENTA,     Material.MAGENTA_WOOL);
        WOOL_BY_COLOR.put(DyeColor.LIGHT_BLUE,  Material.LIGHT_BLUE_WOOL);
        WOOL_BY_COLOR.put(DyeColor.YELLOW,      Material.YELLOW_WOOL);
        WOOL_BY_COLOR.put(DyeColor.LIME,        Material.LIME_WOOL);
        WOOL_BY_COLOR.put(DyeColor.PINK,        Material.PINK_WOOL);
        WOOL_BY_COLOR.put(DyeColor.GRAY,        Material.GRAY_WOOL);
        WOOL_BY_COLOR.put(DyeColor.LIGHT_GRAY,  Material.LIGHT_GRAY_WOOL);
        WOOL_BY_COLOR.put(DyeColor.CYAN,        Material.CYAN_WOOL);
        WOOL_BY_COLOR.put(DyeColor.PURPLE,      Material.PURPLE_WOOL);
        WOOL_BY_COLOR.put(DyeColor.BLUE,        Material.BLUE_WOOL);
        WOOL_BY_COLOR.put(DyeColor.BROWN,       Material.BROWN_WOOL);
        WOOL_BY_COLOR.put(DyeColor.GREEN,       Material.GREEN_WOOL);
        WOOL_BY_COLOR.put(DyeColor.RED,         Material.RED_WOOL);
        WOOL_BY_COLOR.put(DyeColor.BLACK,       Material.BLACK_WOOL);
    }

    public static Material woolOf(DyeColor dye) {
        return WOOL_BY_COLOR.get(dye);          // jamais null
    }
}

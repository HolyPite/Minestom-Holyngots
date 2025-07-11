package org.example.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.CustomModelData;
import org.example.items.datas.Category;
import org.example.items.datas.Rarity;
import org.example.items.datas.StatMap;
import org.example.items.datas.StatType;
import org.example.utils.TKit;

import java.util.*;

public final class GameItem {

    /* -------- champs immuables -------- */
    public final String id;
    public final Component displayName;
    public final Rarity rarity;
    public final Category category;
    public final boolean tradable;
    public final String customModel;
    public final Material material;
    public final StatMap stats;
    public final List<String> story;
    public final int maxStack;

    private GameItem(Builder b) {
        this.id          = b.id;
        this.displayName = b.displayName;
        this.rarity      = b.rarity;
        this.category    = b.category;
        this.tradable    = b.tradable;
        this.customModel = id;
        this.material    = b.material;
        this.stats       = b.stats;
        this.story       = List.copyOf(b.story);
        this.maxStack    = b.maxStack;
    }

    /* -------- conversion -------- */
    public ItemStack toItemStack() {
        return ItemStack.of(material)
                .with(DataComponents.CUSTOM_NAME,  displayName)
                .with(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(customModel), List.of()))
                .with(DataComponents.LORE,          buildLore())
                .with(DataComponents.MAX_STACK_SIZE, maxStack)
                /* ------------- SUPPRIME les attributs vanilla ------------- */
                .with(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(List.of()))
                .withoutExtraTooltip();
    }

    /* -------- lore -------- */
    private List<Component> buildLore() {
        List<Component> lore = new ArrayList<>();

        if (!story.isEmpty()) {
            story.forEach(line -> lore.add(Component.text(line,
                    NamedTextColor.GRAY, TextDecoration.ITALIC)));
            lore.add(Component.empty());
        }

        lore.add(Component.text(rarity.name(),rarity.color, TextDecoration.BOLD));
        lore.add(Component.text(category.name(), NamedTextColor.GRAY));
        lore.add(Component.empty());

        stats.forEach((stat, value) -> {

            /* ---------- 1) label en or ---------- */
            Component line = Component.text(" • ", NamedTextColor.GRAY);
            line = line.append(Component.text(stat.label + " :", NamedTextColor.GRAY).decorate(TextDecoration.UNDERLINED));

            /* ---------- 2) valeur + signe / % ---------- */
            String valueStr = switch (stat.kind) {
                case FLAT     -> " %s%d".formatted(value > 0 ? "+" : "", value);
                case PROBA    ->              " %d%%".formatted(value);                 // jamais de signe
                case PERCENT  -> " %s%d%%".formatted(value > 0 ? "+" : "", value);
            };

            NamedTextColor color = ((value >= 0) ? NamedTextColor.WHITE : NamedTextColor.RED);
            line = line.append(Component.text(valueStr, color));
            lore.add(line);
        });



        if (!tradable) {
            lore.add(Component.empty());
            lore.add(TKit.createGradientText("Ne peut pas être échangé",
                    TextColor.color(0xFF5151),
                    TextColor.color(0xFF9EA3))
                    .decorate(TextDecoration.ITALIC));
        }
        return lore;
    }

    /* -------- Builder -------- */
    public static final class Builder {
        private final String id;
        private final Component displayName;
        private final String customModel;

        private Rarity   rarity   = Rarity.USELESS;
        private Category category = Category.MISC;
        private boolean  tradable = true;
        private Material material = Material.STICK;
        private final StatMap stats = new StatMap();
        private List<String> story = Collections.emptyList();
        private int maxStack = 64;                 // ← NEW

        public Builder(String id, Component name) {
            this.id = id;
            this.displayName = name;
            this.customModel = id;
        }

        public Builder rarity(Rarity r)            { this.rarity = r;   return this; }
        public Builder category(Category c)        { this.category = c; return this; }
        public Builder tradable(boolean t)         { this.tradable = t; return this; }
        public Builder material(Material m)        { this.material = m; return this; }
        public Builder stat(StatType t, int v)     { this.stats.with(t, v); return this; }

        public Builder story(String... lines)      { this.story = Arrays.asList(lines); return this; }
        public Builder story(List<String> lines)   { this.story = List.copyOf(lines);   return this; }

        public Builder stackSize(int n)            { this.maxStack = Math.max(1, Math.min(n, 64)); return this; }

        public GameItem build()                    { return new GameItem(this); }
    }
}

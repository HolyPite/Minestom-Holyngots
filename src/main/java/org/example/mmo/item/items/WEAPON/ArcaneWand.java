package org.example.mmo.item.items.WEAPON;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.example.mmo.item.datas.AmmoType;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.GameItem.ProjectileOptions.Trigger;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatType;

public final class ArcaneWand {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("arcane_wand",
                Component.text("Baguette Arcanique", NamedTextColor.LIGHT_PURPLE))
                .rarity(Rarity.EPIC)
                .category(Category.WEAPON)
                .material(Material.BLAZE_ROD)
                .stat(StatType.ATTACK, 16)
                .stat(StatType.CRIT_CHANCE, 12)
                .stat(StatType.CRIT_VALUE, 35)
                .story("Canalise une rafale de magie pure.")
                .stackSize(1)
                .projectile(options -> options
                        .trigger(Trigger.RIGHT_CLICK)
                        .projectileType(EntityType.SMALL_FIREBALL)
                        .speed(1.6D)
                        .spread(0.03D)
                        .hasGravity(false)
                        .cooldownTicks(20L)
                        .allowOffHand(true)
                        .ammoRequirement(AmmoType.MAGIC, 1))
                .build();

        ItemRegistry.register(ITEM);
    }

    private ArcaneWand() {
    }
}

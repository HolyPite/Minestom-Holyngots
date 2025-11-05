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

public final class FrostcasterStaff {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("frostcaster_staff",
                Component.text("Baton Cryomancien", NamedTextColor.AQUA))
                .rarity(Rarity.RARE)
                .category(Category.WEAPON)
                .material(Material.STICK)
                .stat(StatType.ATTACK, 11)
                .stat(StatType.KNOCKBACK, 15)
                .stat(StatType.CRIT_VALUE, 20)
                .story("Canalise des cristaux de glace pour immobiliser les ennemis.")
                .stackSize(1)
                .projectile(options -> options
                        .trigger(Trigger.BOTH)
                        .projectileType(EntityType.SNOWBALL)
                        .speed(1.3D)
                        .spread(0.08D)
                        .hasGravity(true)
                        .cooldownTicks(6L)
                        .range(20.0D)
                        .allowOffHand(true)
                        .ammoRequirement(AmmoType.MAGIC, 1))
                .build();

        ItemRegistry.register(ITEM);
    }

    private FrostcasterStaff() {
    }
}


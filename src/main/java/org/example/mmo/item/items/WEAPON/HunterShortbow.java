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

public final class HunterShortbow {

    public static final GameItem ITEM;

    static {
        ITEM = new GameItem.Builder("hunter_shortbow",
                Component.text("Arc de Traqueur", NamedTextColor.GREEN))
                .rarity(Rarity.RARE)
                .category(Category.WEAPON)
                .material(Material.BOW)
                .stat(StatType.ATTACK, 9)
                .stat(StatType.CRIT_CHANCE, 8)
                .stat(StatType.ATTACK_SPEED, 12)
                .story("Un arc court fiable utilise par les chasseurs de la region.")
                .stackSize(1)
                .projectile(options -> options
                        .trigger(Trigger.USE_RELEASE)
                        .chargeTicks(25L)
                        .projectileType(EntityType.ARROW)
                        .speed(2.4D)
                        .spread(0.01D)
                        .hasGravity(true)
                        .cooldownTicks(10L)
                        .allowOffHand(false)
                        .ammoRequirement(AmmoType.ARROW, 1))
                .build();

        ItemRegistry.register(ITEM);
    }

    private HunterShortbow() {
    }
}


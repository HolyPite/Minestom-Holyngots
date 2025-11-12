// org.example.mmo.items.DEBUG.StatsGrimoire
package org.example.mmo.item.items.DEV;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.utils.TKit;
import org.example.mmo.item.*;
import org.example.mmo.item.power.powers.StatsDisplayPower;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

public final class StatsGrimoire {

    public static final GameItem ITEM =
            new GameItem.Builder("stats_grimoire",
                    TKit.createGradientText("Grimoire des Stats", NamedTextColor.GOLD, NamedTextColor.YELLOW))
                    .category(Category.MISC)
                    .rarity(Rarity.UNCOMMON)
                    .material(Material.WRITABLE_BOOK)
                    .tradable(false)
                    .stackSize(1)
                    .story("Clic-droit (air ou bloc) pour","afficher vos attributs actuels.")
                    .skill(StatsDisplayPower.ID, skill -> skill
                            .addTrigger(SkillTrigger.RIGHT_CLICK_AIR)
                            .addTrigger(SkillTrigger.RIGHT_CLICK_BLOCK)
                            .addTrigger(SkillTrigger.INVENTORY_CLICK)
                            .cooldown(Duration.ZERO))
                    .build();

    static {
        ItemRegistry.register(ITEM);
    }

    private StatsGrimoire() {}
}

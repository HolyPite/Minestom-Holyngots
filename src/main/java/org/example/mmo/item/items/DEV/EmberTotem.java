package org.example.mmo.item.items.DEV;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.power.powers.IgniteBurstPower;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

public final class EmberTotem {

    public static final GameItem ITEM = new GameItem.Builder("ember_totem",
            Component.text("Totem Braises", NamedTextColor.GOLD))
            .category(Category.MISC)
            .rarity(Rarity.RARE)
            .material(Material.BLAZE_ROD)
            .tradable(false)
            .stackSize(1)
            .story("Libere un anneau de flammes", "autour de son porteur.")
            .skill(IgniteBurstPower.ID, skill -> skill
                    .addTrigger(SkillTrigger.RIGHT_CLICK_AIR)
                    .addTrigger(SkillTrigger.RIGHT_CLICK_BLOCK)
                    .cooldown(Duration.ofSeconds(12))
                    .parameters(PowerParameters.builder()
                            .put("radius", 4.5)
                            .put("y_offset", -1.0)
                            .build()))
            .build();

    static {
        ItemRegistry.register(ITEM);
    }

    private EmberTotem() {}
}

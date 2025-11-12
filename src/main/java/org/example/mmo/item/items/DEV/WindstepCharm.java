package org.example.mmo.item.items.DEV;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.power.powers.DashPower;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

public final class WindstepCharm {

    public static final GameItem ITEM = new GameItem.Builder("windstep_charm",
            Component.text("Amulette du Vent", NamedTextColor.AQUA))
            .category(Category.MISC)
            .rarity(Rarity.UNCOMMON)
            .material(Material.FEATHER)
            .tradable(true)
            .stackSize(1)
            .story("Une brise captive qui accelere", "vos mouvements.")
            .skill(DashPower.ID, skill -> skill
                    .addTrigger(SkillTrigger.RIGHT_CLICK_AIR)
                    .addTrigger(SkillTrigger.LEFT_CLICK_AIR)
                    .cooldown(Duration.ofSeconds(4))
                    .parameters(PowerParameters.builder()
                            .put("speed", 9.5)
                            .put("vertical_scale", 0.25)
                            .build()))
            .build();

    static {
        ItemRegistry.register(ITEM);
    }

    private WindstepCharm() {}
}

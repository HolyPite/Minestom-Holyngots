package org.example.mmo.item.items.DEV;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.power.powers.DashPower;
import org.example.mmo.item.power.powers.TeleportPower;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

public final class StormrunnerSigil {

    public static final GameItem ITEM = new GameItem.Builder("stormrunner_sigil",
            Component.text("Sceau de l'Orage", NamedTextColor.BLUE))
            .category(Category.MISC)
            .rarity(Rarity.EPIC)
            .material(Material.HEART_OF_THE_SEA)
            .tradable(false)
            .stackSize(1)
            .story("Permet de filer comme l'eclair", "ou de disparaitre dans un flash.")
            .skill(DashPower.ID, skill -> skill
                    .addTrigger(SkillTrigger.LEFT_CLICK_AIR)
                    .level(2)
                    .cooldown(Duration.ofSeconds(3))
                    .parameterTemplate(template -> template
                            .linear("speed", 20.0, 4.0)
                            .constant("vertical_scale", 0.2)))
            .skill(TeleportPower.ID, skill -> skill
                    .addTrigger(SkillTrigger.RIGHT_CLICK_AIR)
                    .cooldown(Duration.ofSeconds(10))
                    .parameters(PowerParameters.builder()
                            .put("distance", 10.0)
                            .put("vertical_offset", 0.2)
                            .build()))
            .build();

    static {
        ItemRegistry.register(ITEM);
    }

    private StormrunnerSigil() {}
}

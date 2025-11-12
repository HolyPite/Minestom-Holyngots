package org.example.mmo.item.items.DEV;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.power.powers.TeleportPower;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;

public final class BlinkRune {

    public static final GameItem ITEM = new GameItem.Builder("blink_rune",
            Component.text("Rune de Transfert", NamedTextColor.LIGHT_PURPLE))
            .category(Category.MISC)
            .rarity(Rarity.RARE)
            .material(Material.ENDER_EYE)
            .tradable(false)
            .stackSize(1)
            .story("Canalise l'energie du vide pour", "vous projeter vers l'avant.")
            .skill(TeleportPower.ID, skill -> skill
                    .addTrigger(SkillTrigger.RIGHT_CLICK_AIR)
                    .addTrigger(SkillTrigger.RIGHT_CLICK_BLOCK)
                    .cooldown(Duration.ofSeconds(8))
                    .parameters(PowerParameters.builder()
                            .put("distance", 12.0)
                            .put("vertical_offset", 0.5)
                            .build()))
            .build();

    static {
        ItemRegistry.register(ITEM);
    }

    private BlinkRune() {}
}

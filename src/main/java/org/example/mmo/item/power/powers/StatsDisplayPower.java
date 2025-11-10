package org.example.mmo.item.power.powers;

import net.minestom.server.entity.Player;
import org.example.mmo.item.datas.Stats;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillTriggerData;

@PowerId(StatsDisplayPower.ID)
public final class StatsDisplayPower implements Power {

    public static final String ID = "core:stats_display";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        Player player = context.asPlayer();
        if (player == null) {
            return;
        }
        Stats.refresh(player);
        if (context.triggerData() instanceof SkillTriggerData.InventoryClickData data) {
            data.event().setCancelled(true);
        }
    }
}

package org.example.mmo.item.skill.power;

import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.SkillTriggerData;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.datas.Stats;

public final class StatsDisplayPower implements Power {

    public static final String ID = "core:stats_display";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        Stats.refresh(context.player());
        if (context.triggerData() instanceof SkillTriggerData.InventoryClickData data) {
            data.event().setCancelled(true);
        }
    }
}

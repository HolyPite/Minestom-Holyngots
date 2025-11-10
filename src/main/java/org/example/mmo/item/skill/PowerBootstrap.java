package org.example.mmo.item.skill;

import org.example.mmo.item.skill.power.StatsDisplayPower;

public final class PowerBootstrap {

    private PowerBootstrap() {}

    public static void init() {
        PowerRegistry.register(StatsDisplayPower.ID, new StatsDisplayPower());
    }
}

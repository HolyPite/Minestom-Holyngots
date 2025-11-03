package org.example.mmo.npc.mob;

import org.example.mmo.npc.mob.demo.BanditArcherMob;
import org.example.mmo.npc.mob.demo.BanditSkirmisherMob;
import org.example.mmo.npc.mob.demo.ForestWolfMob;

public final class MobBootstrap {

    private MobBootstrap() {
    }

    public static void init() {
        ForestWolfMob.register();
        BanditSkirmisherMob.register();
        BanditArcherMob.register();
    }
}

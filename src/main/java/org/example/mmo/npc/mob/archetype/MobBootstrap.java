package org.example.mmo.npc.mob.archetype;

import org.example.mmo.npc.mob.archetype.demo.BanditArcherMob;
import org.example.mmo.npc.mob.archetype.demo.BanditSkirmisherMob;
import org.example.mmo.npc.mob.archetype.demo.ForestWolfMob;

public final class MobBootstrap {

    private MobBootstrap() {
    }

    public static void init() {
        ForestWolfMob.register();
        BanditSkirmisherMob.register();
        BanditArcherMob.register();
    }
}

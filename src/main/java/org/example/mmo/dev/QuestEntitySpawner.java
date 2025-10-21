package org.example.mmo.dev;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.example.mmo.quest.QuestManager;

public class QuestEntitySpawner {

    public static void spawnQuestNpcs(Instance instance, Pos position) {
        // Spawn the guide NPC and tag it correctly for the quest system
        EntityCreature warden = new EntityCreature(EntityType.WARDEN);
        warden.setTag(QuestManager.NPC_ID_TAG, "guide");
        warden.setInstance(instance, position);
    }
}

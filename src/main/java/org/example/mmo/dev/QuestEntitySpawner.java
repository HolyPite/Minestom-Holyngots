package org.example.mmo.dev;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;
import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.QuestManager;

public class QuestEntitySpawner {

    public static void spawnPersistentEntities(InstanceRegistry instances) {
        for (Instance instance : instances.gameInstances()) {
            spawnNpcsForInstance(instance);
        }
    }

    private static void spawnNpcsForInstance(Instance instance) {
        NpcRegistry.all().values().forEach(npc -> {
            EntityCreature creature = new EntityCreature(npc.entityType());

            creature.set(DataComponents.CUSTOM_NAME, npc.name());
            creature.setCustomNameVisible(true);
            creature.setInvulnerable(true);
            creature.setNoGravity(true);

            creature.setTag(QuestManager.NPC_ID_TAG, npc.id());
            creature.setInstance(instance, npc.spawnPosition());
        });
    }
}

package org.example.mmo.dev;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.example.bootstrap.InstanceRegistry;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.QuestManager;

import java.util.concurrent.ThreadLocalRandom;

public class QuestEntitySpawner {

    private static final Pos HUNTING_GROUND_CENTER = new Pos(0, 42, 15);

    public static void spawnPersistentEntities(InstanceRegistry instances) {
        for (Instance instance : instances.gameInstances()) {
            spawnNpcsForInstance(instance);
            spawnMonstersForInstance(instance);
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

    private static void spawnMonstersForInstance(Instance instance) {
        spawnGroup(instance, EntityType.ZOMBIE, 5, HUNTING_GROUND_CENTER, 5);
        spawnGroup(instance, EntityType.SKELETON, 5, HUNTING_GROUND_CENTER, 5);
        spawnGroup(instance, EntityType.SPIDER, 2, HUNTING_GROUND_CENTER, 5);
    }

    private static void spawnGroup(Instance instance, EntityType type, int count, Pos center, double radius) {
        for (int i = 0; i < count; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double angle = random.nextDouble(0, 2 * Math.PI);
            double distance = random.nextDouble(0, radius);

            double x = center.x() + distance * Math.cos(angle);
            double z = center.z() + distance * Math.sin(angle);

            EntityCreature creature = new EntityCreature(type);
            creature.setInstance(instance, new Pos(x, center.y(), z));
        }
    }
}

package org.example.mmo.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.combat.util.HealthUtils;
import org.example.mmo.mob.behaviour.MobBehaviour;
import org.example.mmo.mob.behaviour.MobBehaviourFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles mob instantiation, equipment application and lifecycle bookkeeping.
 */
public final class MobSpawnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobSpawnService.class);
    private final Map<UUID, MobInstance> activeMobs = new ConcurrentHashMap<>();

    public MobInstance spawn(MobArchetype archetype, Instance instance, Pos position) {
        LivingEntity entity = archetype.entityFactory().create();
        ensureChunkLoaded(instance, position);
        entity.setInstance(instance, position);
        entity.setTag(MobMetadataKeys.ARCHETYPE_ID, archetype.id());

        applyStats(archetype, entity);
        applyEquipment(archetype, entity);
        applyCustomName(archetype, entity);
        applyAi(archetype, entity);

        List<MobBehaviour> behaviours = instantiateBehaviours(archetype, entity);
        MobInstance mobInstance = new MobInstance(archetype, instance, entity.getUuid(), behaviours);
        activeMobs.put(entity.getUuid(), mobInstance);

        MobAiService.track(mobInstance);
        return mobInstance;
    }

    public Optional<MobInstance> get(UUID entityUuid) {
        return Optional.ofNullable(activeMobs.get(entityUuid));
    }

    public void remove(UUID entityUuid) {
        activeMobs.remove(entityUuid);
        MobAiService.untrack(entityUuid);
    }

    private void applyStats(MobArchetype archetype, LivingEntity entity) {
        Double health = archetype.stats().get(StatType.HEALTH);
        if (health != null && health > 0) {
            var attribute = entity.getAttribute(Attribute.MAX_HEALTH);
            if (attribute != null) {
                attribute.setBaseValue(health);
            }
            entity.setHealth(health.floatValue());
        }
        // TODO: map other stats to attributes when the attribute model is finalised
    }

    private void applyEquipment(MobArchetype archetype, LivingEntity entity) {
        MobEquipment equipment = archetype.equipment();
        if (equipment.isEmpty()) {
            return;
        }
        for (Map.Entry<EquipmentSlot, String> entry : equipment.equipment().entrySet()) {
            GameItem gameItem = ItemRegistry.byId(entry.getValue());
            if (gameItem == null) {
                LOGGER.warn("Unknown game item '{}' for mob '{}'", entry.getValue(), archetype.id());
                continue;
            }
            ItemStack stack = gameItem.toItemStack();
            entity.setEquipment(entry.getKey(), stack);
        }
    }

    private void applyCustomName(MobArchetype archetype, LivingEntity entity) {
        entity.setTag(MobMetadataKeys.DISPLAY_NAME, archetype.displayName());
        HealthUtils.updateHealthBar(entity);
    }

    private void applyAi(MobArchetype archetype, LivingEntity entity) {
        archetype.aiFactory().ifPresent(factory -> {
            if (!(entity instanceof EntityAI aiEntity)) {
                LOGGER.warn("Mob '{}' uses AI but entity type {} does not implement EntityAI", archetype.id(), entity.getClass().getName());
                return;
            }
            try {
                EntityAIGroup aiGroup = factory.build(entity);
                if (aiGroup != null) {
                    aiEntity.addAIGroup(aiGroup);
                }
            } catch (Exception exception) {
                LOGGER.error("Failed to configure AI for mob {}", archetype.id(), exception);
            }
        });
    }

    private void ensureChunkLoaded(Instance instance, Pos position) {
        int chunkX = chunkCoordinate(position.x());
        int chunkZ = chunkCoordinate(position.z());
        instance.loadChunk(chunkX, chunkZ).join();
        instance.loadChunk(chunkX + 1, chunkZ).join();
        instance.loadChunk(chunkX - 1, chunkZ).join();
        instance.loadChunk(chunkX, chunkZ + 1).join();
        instance.loadChunk(chunkX, chunkZ - 1).join();
        instance.loadChunk(chunkX + 1, chunkZ + 1).join();
        instance.loadChunk(chunkX - 1, chunkZ - 1).join();
        instance.loadChunk(chunkX + 1, chunkZ - 1).join();
        instance.loadChunk(chunkX - 1, chunkZ + 1).join();
    }

    private int chunkCoordinate(double coordinate) {
        int block = (int) Math.floor(coordinate);
        return block >> 4;
    }

    private List<MobBehaviour> instantiateBehaviours(MobArchetype archetype, LivingEntity entity) {
        List<MobBehaviour> behaviours = new ArrayList<>();
        for (MobBehaviourFactory factory : archetype.behaviourFactories()) {
            try {
                behaviours.add(factory.create(archetype, entity));
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate behaviour for mob {}", archetype.id(), e);
            }
        }
        return behaviours;
    }
}

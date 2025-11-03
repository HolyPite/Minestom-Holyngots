package org.example.mmo.npc.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.example.bootstrap.GameContext;
import org.example.mmo.npc.mob.behaviour.MobBehaviour;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wires mob behaviours into the Minestom event graph.
 */
public final class MobAiService {

    private static final Map<UUID, MobInstance> ACTIVE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MobAiService.class);

    private MobAiService() {
    }

    public static void init(EventNode<EntityEvent> entityNode) {
        entityNode.addListener(EntityTickEvent.class, event -> {
            MobInstance instance = ACTIVE.get(event.getEntity().getUuid());
            if (instance == null) {
                return;
            }
            long tick = event.getEntity().getAliveTicks();
            invoke(instance.behaviours(), behaviour -> behaviour.onTick(instance, tick));
        });

        entityNode.addListener(EntityDamageEvent.class, event -> {
            MobInstance instance = ACTIVE.get(event.getEntity().getUuid());
            if (instance == null) {
                return;
            }
            Damage damage = event.getDamage();
            if (damage != null) {
                Entity attacker = damage.getAttacker();
                if (attacker != null) {
                    invoke(instance.behaviours(), behaviour -> behaviour.onAggro(instance, attacker));
                }
                invoke(instance.behaviours(), behaviour -> behaviour.onDamaged(instance, damage));
            }
        });

        entityNode.addListener(EntityDeathEvent.class, event -> {
            UUID uuid = event.getEntity().getUuid();
            MobInstance instance = ACTIVE.remove(uuid);
            if (instance == null) {
                return;
            }
            Entity entity = event.getEntity();
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }
            Damage lastDamage = livingEntity.getLastDamageSource();
            Entity killer = lastDamage != null ? lastDamage.getAttacker() : null;
            invoke(instance.behaviours(), behaviour -> behaviour.onDeath(instance, killer));
            invoke(instance.behaviours(), behaviour -> behaviour.onCleanup(instance));
            GameContext.get().mobSpawnService().remove(uuid);
        });
    }

    public static void track(MobInstance instance) {
        ACTIVE.put(instance.entityUuid(), instance);
        invoke(instance.behaviours(), behaviour -> behaviour.onSpawn(instance));
    }

    public static void untrack(UUID entityUuid) {
        MobInstance instance = ACTIVE.remove(entityUuid);
        if (instance != null) {
            invoke(instance.behaviours(), behaviour -> behaviour.onCleanup(instance));
        }
    }

    private static void invoke(List<MobBehaviour> behaviours, BehaviourConsumer consumer) {
        for (MobBehaviour behaviour : behaviours) {
            try {
                consumer.accept(behaviour);
            } catch (Throwable throwable) {
                LOGGER.error("Mob behaviour invocation failed", throwable);
            }
        }
    }

    @FunctionalInterface
    private interface BehaviourConsumer {
        void accept(MobBehaviour behaviour);
    }
}

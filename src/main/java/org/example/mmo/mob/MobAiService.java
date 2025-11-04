package org.example.mmo.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.example.bootstrap.GameContext;
import org.example.data.data_class.PlayerData;
import org.example.mmo.combat.history.DamageHistory;
import org.example.mmo.combat.history.DamageRecord;
import org.example.mmo.combat.history.DamageTracker;
import org.example.mmo.item.ItemDelivery;
import org.example.mmo.mob.behaviour.MobBehaviour;
import org.example.mmo.mob.loot.MobLootBundles;
import org.example.mmo.mob.loot.MobQuestLootRoller;

import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
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
            distributeLoot(instance, livingEntity, killer);
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

    private static void distributeLoot(MobInstance instance, LivingEntity deadEntity, Entity killer) {
        var dropInstance = deadEntity.getInstance();
        var dropPosition = deadEntity.getPosition();
        DamageHistory history = DamageTracker.getHistory(deadEntity);
        if (history == null) {
            if (killer instanceof Player player) {
                giveQuestLoot(instance, player, dropInstance, dropPosition);
                giveBundle(instance, player, dropInstance, dropPosition);
            }
            return;
        }

        List<DamageRecord> records = history.getRecords();
        if (records.isEmpty()) {
            if (killer instanceof Player player) {
                giveQuestLoot(instance, player, dropInstance, dropPosition);
                giveBundle(instance, player, dropInstance, dropPosition);
            }
            return;
        }

        Map<Player, Double> contributions = new HashMap<>();
        double totalDamage = 0d;
        for (DamageRecord record : records) {
            Damage damage = record.damage();
            if (damage == null) {
                continue;
            }
            float amount = damage.getAmount();
            if (amount <= 0f) {
                continue;
            }
            totalDamage += amount;
            Entity attacker = damage.getAttacker();
            if (attacker instanceof Player player) {
                contributions.merge(player, (double) amount, Double::sum);
            }
        }

        if (totalDamage <= 0d || contributions.isEmpty()) {
            return;
        }

        double threshold = instance.archetype().lootContributionThreshold();
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Entry<Player, Double> entry : contributions.entrySet()) {
            Player player = entry.getKey();
            if (player == null || !player.isOnline()) {
                continue;
            }
            double share = entry.getValue() / totalDamage;
            if (share >= threshold) {
                eligiblePlayers.add(player);
            }
        }

        if (eligiblePlayers.isEmpty()) {
            return;
        }

        for (Player player : eligiblePlayers) {
            giveQuestLoot(instance, player, dropInstance, dropPosition);
            giveBundle(instance, player, dropInstance, dropPosition);
        }
    }

    private static void giveQuestLoot(MobInstance instance,
                                      Player player,
                                      net.minestom.server.instance.Instance dropInstance,
                                      net.minestom.server.coordinate.Pos dropPosition) {
        var questTable = instance.archetype().questLootTable();
        if (questTable == null || questTable.isEmpty()) {
            return;
        }
        PlayerData data = GameContext.get().playerDataService().get(player);
        if (data == null) {
            return;
        }
        var drops = MobQuestLootRoller.generateLoot(questTable, player, data, ThreadLocalRandom.current());
        for (var stack : drops) {
            ItemDelivery.giveOrDrop(player, stack, dropInstance, dropPosition);
        }
    }

    private static void giveBundle(MobInstance instance,
                                   Player player,
                                   net.minestom.server.instance.Instance dropInstance,
                                   net.minestom.server.coordinate.Pos dropPosition) {
        var bundle = MobLootBundles.createBundleStack(instance.archetype());
        if (bundle.isAir()) {
            return;
        }
        ItemDelivery.giveOrDrop(player, bundle, dropInstance, dropPosition);
    }
}

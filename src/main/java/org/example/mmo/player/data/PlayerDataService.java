package org.example.mmo.player.data;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.timer.TaskSchedule;
import org.example.bootstrap.InstanceRegistry;
import org.example.data.data_class.ItemData;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.ItemUtils;
import org.example.utils.ToastManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerDataService {

    private final PlayerDataRepository repository;
    private final InstanceRegistry instances;

    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final Map<UUID, Set<Instance>> currentGroup = new HashMap<>();

    private static final long DEFAULT_AUTOSAVE_MINUTES = 1;
    private static final int BASE_EXPERIENCE_PER_LEVEL = 100;
    private static final int EXPERIENCE_GROWTH_PER_LEVEL = 50;

    public PlayerDataService(PlayerDataRepository repository, InstanceRegistry instances) {
        this.repository = repository;
        this.instances = instances;
    }

    public void init(EventNode<Event> root) {
        EventNode<PlayerEvent> playerNode =
                root.findChildren("playerNode", PlayerEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode =
                root.findChildren("inventoryNode", InventoryEvent.class).getFirst();

        MinecraftServer.getGlobalEventHandler().addListener(AddEntityToInstanceEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            Set<Instance> newGroup = instances.allInstanceGroups().stream()
                    .filter(g -> g.contains(event.getInstance()))
                    .findFirst()
                    .orElse(null);

            Set<Instance> oldGroup = currentGroup.get(player.getUuid());

            if (Objects.equals(newGroup, oldGroup)) {
                return;
            }

            if (oldGroup != null) {
                PlayerData oldData = cache.remove(player.getUuid());
                if (oldData != null) {
                    repository.save(oldData, oldGroup);
                }
            }

            if (newGroup != null) {
                PlayerData newData = repository.load(player.getUuid(), newGroup);
                cache.put(player.getUuid(), newData);

                PlayerInventory inv = player.getInventory();
                inv.clear();
                for (ItemData item : newData.inventory) {
                    ItemStack stack = dataToItem(item);
                    if (item.slot >= 0 && item.slot < inv.getSize()) {
                        inv.setItemStack(item.slot, stack);
                    } else {
                        inv.addItemStack(stack);
                    }
                }
                currentGroup.put(player.getUuid(), newGroup);
            } else {
                currentGroup.remove(player.getUuid());
            }
        });

        playerNode.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            Set<Instance> group = currentGroup.remove(player.getUuid());
            PlayerData data = cache.remove(player.getUuid());
            if (data != null && group != null) {
                updateInventory(player, data);
                updatePosition(player, data);
                repository.save(data, group);
            }
        });

        inventoryNode.addListener(InventoryItemChangeEvent.class, e -> {
            if (e.getInventory() instanceof PlayerInventory inv) {
                for (Player viewer : inv.getViewers()) {
                    PlayerData data = cache.get(viewer.getUuid());
                    if (data != null) {
                        updateInventory(viewer, data);
                    }
                }
            }
        });
    }

    public PlayerData get(Player player) {
        return cache.get(player.getUuid());
    }

    public void savePlayer(Player player) {
        PlayerData data = cache.get(player.getUuid());
        if (data == null) {
            return;
        }
        updateInventory(player, data);
        updatePosition(player, data);
        Set<Instance> group = currentGroup.get(player.getUuid());
        if (group != null) {
            repository.save(data, group);
        }
    }

    public void startAutoSave() {
        startAutoSave(DEFAULT_AUTOSAVE_MINUTES);
    }

    public void startAutoSave(long minutes) {
        MinecraftServer.getSchedulerManager().buildTask(this::saveAll)
                .delay(TaskSchedule.minutes(minutes))
                .repeat(TaskSchedule.minutes(minutes))
                .schedule();
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::saveAll);
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }

    public void saveAll() {
        cache.forEach((uuid, data) -> {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
            if (player != null) {
                updateInventory(player, data);
                updatePosition(player, data);
            }
            Set<Instance> group = currentGroup.get(uuid);
            if (group != null) {
                repository.save(data, group);
            }
        });
    }

    private void updateInventory(Player player, PlayerData data) {
        PlayerInventory inventory = player.getInventory();
        data.inventory.clear();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItemStack(slot);
            if (!stack.isAir()) {
                data.inventory.add(stackToItemData(stack, slot));
            }
        }
    }

    private void updatePosition(Player player, PlayerData data) {
        data.position = player.getPosition();
        if (player.getInstance() instanceof InstanceContainer container) {
            data.lastInstance = instances.nameOf(container);
        }
    }

    private ItemData stackToItemData(ItemStack stack, int slot) {
        String gameItemId = ItemUtils.getId(stack);
        String identifier = gameItemId != null ? gameItemId : stack.material().name();
        return new ItemData(identifier, stack.amount(), slot);
    }

    private ItemStack dataToItem(ItemData data) {
        GameItem gameItem = ItemRegistry.byId(data.itemId);
        if (gameItem != null) {
            return gameItem.toItemStack().withAmount(data.amount);
        }
        Material material = resolveMaterial(data.itemId);
        return material != null ? ItemStack.of(material, data.amount) : ItemStack.AIR;
    }

    private Material resolveMaterial(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        Material material = Material.fromKey(rawId);
        if (material != null) {
            return material;
        }

        String lower = rawId.toLowerCase(Locale.ROOT);
        material = Material.fromKey(lower.contains(":") ? lower : "minecraft:" + lower);
        if (material != null) {
            return material;
        }

        for (Material candidate : Material.values()) {
            if (rawId.equalsIgnoreCase(candidate.name())) {
                return candidate;
            }
        }
        return null;
    }

    public void grantExperience(Player player, int amount) {
        if (player == null || amount <= 0) {
            return;
        }

        PlayerData data = cache.get(player.getUuid());
        if (data == null) {
            return;
        }

        int remaining = amount;
        int levelsGained = 0;

        while (remaining > 0) {
            int levelRequirement = experienceRequiredForLevel(data.level);
            int xpNeeded = Math.max(levelRequirement - data.experience, 0);
            if (xpNeeded == 0) {
                xpNeeded = levelRequirement;
                data.experience = 0;
            }

            if (remaining >= xpNeeded) {
                remaining -= xpNeeded;
                data.experience = 0;
                data.level++;
                levelsGained++;
            } else {
                data.experience += remaining;
                remaining = 0;
            }
        }

        Component gained = Component.text("+" + amount + " EXP", NamedTextColor.GREEN);
        ToastManager.showToast(player, gained, Material.EXPERIENCE_BOTTLE, FrameType.GOAL);

        if (levelsGained > 0) {
            Component levelUp = Component.text("Niveau " + data.level, NamedTextColor.GOLD);
            ToastManager.showToast(player, levelUp, Material.NETHER_STAR, FrameType.CHALLENGE);
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER, 0.9f, 1.1f), player.getPosition());
        }
    }

    private int experienceRequiredForLevel(int level) {
        int clampedLevel = Math.max(level, 1);
        return BASE_EXPERIENCE_PER_LEVEL + (clampedLevel - 1) * EXPERIENCE_GROWTH_PER_LEVEL;
    }
}

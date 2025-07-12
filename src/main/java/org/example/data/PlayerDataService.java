package org.example.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import org.example.InstancesInit;
import org.example.data.data_class.ItemData;
import org.example.data.data_class.PlayerData;
import org.example.mmo.items.GameItem;
import org.example.mmo.items.ItemRegistry;
import org.example.mmo.items.ItemUtils;
import org.example.mmo.items.datas.Stats;

import java.util.*;

/**
 * Handles loading and saving player data for different instance groups.
 */
public class PlayerDataService {
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final Map<UUID, String> activeGroups = new HashMap<>();
    private final Map<String, Set<Instance>> groups;

    private static final long DEFAULT_AUTOSAVE_MINUTES = 1;

    public PlayerDataService(PlayerDataRepository repository, Map<String, Set<Instance>> groups) {
        this.repository = repository;
        this.groups = groups;
    }

    public void init(EventNode<Event> events) {
        MinecraftServer.getGlobalEventHandler().addListener(AddEntityToInstanceEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) return;

            String previous = activeGroups.get(player.getUuid());
            String next = InstancesInit.getGroup(event.getInstance());

            if (Objects.equals(previous, next)) return;

            PlayerData data = cache.computeIfAbsent(player.getUuid(), repository::load);

            if (previous != null) {
                saveInventory(player, data, previous);
            }
            if (next != null) {
                loadInventory(player, data, next);
                activeGroups.put(player.getUuid(), next);
            } else {
                activeGroups.remove(player.getUuid());
                repository.save(data);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = cache.remove(player.getUuid());
            String group = activeGroups.remove(player.getUuid());
            if (data != null) {
                if (group != null) saveInventory(player, data, group);
                repository.save(data);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryItemChangeEvent.class, e -> {
            if (e.getInventory() instanceof PlayerInventory inv) {
                for (Player viewer : inv.getViewers()) {
                    PlayerData data = cache.get(viewer.getUuid());
                    String group = activeGroups.get(viewer.getUuid());
                    if (data != null && group != null) {
                        saveInventory(viewer, data, group);
                    }
                }
            }
        });
    }

    public PlayerData get(Player player) {
        return cache.get(player.getUuid());
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                            */
    /* ------------------------------------------------------------------ */

    private void saveInventory(Player player, PlayerData data, String group) {
        List<ItemData> list = new ArrayList<>();
        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack stack = inv.getItemStack(slot);
            if (!stack.isAir()) {
                list.add(itemToData(stack, slot));
            }
        }
        data.inventories.put(group, list);
    }

    private void loadInventory(Player player, PlayerData data, String group) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        List<ItemData> list = data.inventories.get(group);
        if ((list == null || list.isEmpty()) && group.equals("game") && !data.inventory.isEmpty()) {
            list = data.inventory;
        }
        if (list != null) {
            for (ItemData itemData : list) {
                if (itemData.slot >= 0 && itemData.slot < inv.getSize()) {
                    inv.setItemStack(itemData.slot, dataToItem(itemData));
                } else {
                    inv.addItemStack(dataToItem(itemData));
                }
            }
        }
        Stats.refresh(player);
    }

    private ItemData itemToData(ItemStack stack, int slot) {
        GameItem gi = ItemUtils.resolve(stack);
        String id = gi != null ? gi.id : stack.material().name();
        return new ItemData(id, stack.amount(), slot);
    }

    private ItemStack dataToItem(ItemData data) {
        GameItem gi = ItemRegistry.byId(data.itemId);
        if (gi != null) {
            return gi.toItemStack().withAmount(data.amount);
        }
        Material mat = Material.fromKey(data.itemId);
        if (mat == null) mat = Material.AIR;
        return ItemStack.of(mat).withAmount(data.amount);
    }

    /** Starts the periodic auto-save task using the default interval. */
    public void startAutoSave() {
        startAutoSave(DEFAULT_AUTOSAVE_MINUTES);
    }

    /** Starts the periodic auto-save task using the specified interval in minutes. */
    public void startAutoSave(long minutes) {
        MinecraftServer.getSchedulerManager().buildTask(this::saveAll)
                .delay(TaskSchedule.minutes(minutes))
                .repeat(TaskSchedule.minutes(minutes))
                .schedule();
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::saveAll);
        registerShutdownHook();
    }

    /** Registers a shutdown hook to persist all player data. */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }

    /** Saves all cached player data to the repository. */
    public void saveAll() {
        cache.forEach((uuid, data) -> {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
            String group = activeGroups.get(uuid);
            if (player != null && group != null) {
                saveInventory(player, data, group);
            }
            repository.save(data);
        });
    }
}

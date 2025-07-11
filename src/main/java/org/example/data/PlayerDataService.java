package org.example.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.data.data_class.PlayerData;
import org.example.items.GameItem;
import org.example.items.ItemRegistry;
import org.example.items.ItemUtils;
import org.example.data.data_class.ItemData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles loading and saving player data when players connect or disconnect.
 */
public class PlayerDataService {
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    private static final long DEFAULT_AUTOSAVE_MINUTES = 2;

    public PlayerDataService(PlayerDataRepository repository) {
        this.repository = repository;
    }

    public void init(GlobalEventHandler events) {
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = repository.load(player.getUuid());
            cache.put(player.getUuid(), data);

            // Apply saved stats to the player
            //player.setLevel(data.level);
            //player.setExp(data.experience / 100f);

            PlayerInventory inv = player.getInventory();
            inv.clear();
            for (ItemData itemData : data.inventory) {
                if (itemData.slot >= 0 && itemData.slot < inv.getSize()) {
                    inv.setItemStack(itemData.slot, dataToItem(itemData));
                } else {
                    inv.addItemStack(dataToItem(itemData));
                }
            }
        });

        // Keep cache updated when the player inventory changes
        events.addListener(InventoryItemChangeEvent.class, e -> {
            if (e.getInventory() instanceof PlayerInventory inv) {
                for (Player viewer : inv.getViewers()) {
                    PlayerData data = cache.get(viewer.getUuid());
                    if (data != null) {
                        updateInventory(viewer, data);
                    }
                }
            }
        });

        /* Update experience and level when picking up xp orbs
        events.addListener(PickupExperienceEvent.class, e -> {
            Player player = e.getPlayer();
            PlayerData data = cache.get(player.getUuid());
            if (data != null) {
                data.level = player.getLevel();
                data.experience = Math.round(player.getExp() * 100);
            }
        });*/

        events.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = cache.remove(player.getUuid());
            if (data != null) {
                // Sync inventory/exp one last time before saving
                updateInventory(player, data);
                //data.level = player.getLevel();
                //data.experience = Math.round(player.getExp() * 100);
                repository.save(data);
            }
        });

    }

    public PlayerData get(Player player) {
        return cache.get(player.getUuid());
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                            */
    /* ------------------------------------------------------------------ */

    private void updateInventory(Player player, PlayerData data) {
        data.inventory.clear();
        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack stack = inv.getItemStack(slot);
            if (!stack.isAir()) {
                data.inventory.add(itemToData(stack, slot));
            }
        }
    }

    private ItemData itemToData(ItemStack stack) {
        return itemToData(stack, -1);
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
    }

    /** Saves all cached player data to the repository. */
    public void saveAll() {
        cache.forEach((uuid, data) -> {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
            if (player != null) {
                updateInventory(player, data);
            }
            repository.save(data);
        });
    }
}

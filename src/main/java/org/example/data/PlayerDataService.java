package org.example.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.InstancesInit;
import org.example.data.data_class.PlayerData;
import org.example.mmo.items.GameItem;
import org.example.mmo.items.ItemRegistry;
import org.example.mmo.items.ItemUtils;
import org.example.data.data_class.ItemData;
import org.example.mmo.items.datas.Stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles loading and saving player data when players enter or leave the game instances.
 */
public class PlayerDataService {
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final Set<UUID> activePlayers = new HashSet<>();

    private static final long DEFAULT_AUTOSAVE_MINUTES = 1;

    public PlayerDataService(PlayerDataRepository repository) {
        this.repository = repository;
    }


    public void init(EventNode<Event> events) {
        Set<Instance> groupInstances = InstancesInit.GAME_INSTANCES;

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode = events.findChildren("inventoryNode",InventoryEvent.class).getFirst();

        MinecraftServer.getGlobalEventHandler().addListener(AddEntityToInstanceEvent.class, event -> {
            //System.out.println("1");
            if (!(event.getEntity() instanceof Player player)) return;
            //System.out.println("2");

            boolean wasInGroup = activePlayers.contains(player.getUuid());
            boolean nowInGroup = groupInstances.contains(event.getInstance());

            //System.out.println(wasInGroup);
            //System.out.println(nowInGroup);

            if (nowInGroup && !wasInGroup) {
                //System.out.println("3");
                PlayerData data = repository.load(player.getUuid());
                cache.put(player.getUuid(), data);

                PlayerInventory inv = player.getInventory();
                inv.clear();
                for (ItemData itemData : data.inventory) {
                    if (itemData.slot >= 0 && itemData.slot < inv.getSize()) {
                        inv.setItemStack(itemData.slot, dataToItem(itemData));
                    } else {
                        inv.addItemStack(dataToItem(itemData));
                    }
                }

                Stats.refresh(player);
            }

            if (!nowInGroup && wasInGroup) {
                //System.out.println("4");
                PlayerData data = cache.remove(player.getUuid());
                if (data != null) {
                    updateInventory(player, data);
                    repository.save(data);
                }
            }

            if (nowInGroup) {
                //System.out.println("5");
                activePlayers.add(player.getUuid());
            } else {
                //System.out.println("6");
                activePlayers.remove(player.getUuid());
            }
        });

        playerNode.addListener(PlayerDisconnectEvent.class, event -> {
            //System.out.println("7");
            Player player = event.getPlayer();
            PlayerData data = cache.remove(player.getUuid());
            activePlayers.remove(player.getUuid());
            if (data != null) {
                //System.out.println("8");
                updateInventory(player, data);
                repository.save(data);
            }
        });


        // Keep cache updated when the player inventory changes
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
            if (player != null) {
                updateInventory(player, data);
            }
            repository.save(data);
        });
    }
}
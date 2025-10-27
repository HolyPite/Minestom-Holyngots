package org.example.data;

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
import net.minestom.server.timer.TaskSchedule;
import org.example.InstancesInit;
import org.example.data.data_class.ItemData;
import org.example.data.data_class.PlayerData;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.ItemUtils;

import java.util.*;

/**
 * Charge / sauvegarde les données des joueurs et
 * maintient un inventaire distinct pour chaque type d’instance (games, builds…).
 */
public class PlayerDataService {

    /* ------------------------------------------------------------------ */
    /* Champs d’état                                                      */
    /* ------------------------------------------------------------------ */

    private final PlayerDataRepository repository;

    /** Données actuellement en mémoire par joueur. */
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    /** Groupe d’instances (games, builds, …) dans lequel se trouve le joueur. */
    private final Map<UUID, Set<Instance>> currentGroup = new HashMap<>();

    private static final long DEFAULT_AUTOSAVE_MINUTES = 1;

    public PlayerDataService(PlayerDataRepository repository) {
        this.repository = repository;
    }

    /* ------------------------------------------------------------------ */
    /* Initialisation                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * Branche les listeners nécessaires sur le {@code EventNode} donné
     * (on part du principe qu’il contient déjà playerNode & inventoryNode).
     */
    public void init(EventNode<Event> root) {

        // Sous-nœuds déjà créés dans NodesManagement
        EventNode<PlayerEvent> playerNode =
                root.findChildren("playerNode", PlayerEvent.class).getFirst();
        EventNode<InventoryEvent> inventoryNode =
                root.findChildren("inventoryNode", InventoryEvent.class).getFirst();

        /* ---------------- Changement d’instance ---------------- */
        MinecraftServer.getGlobalEventHandler().addListener(AddEntityToInstanceEvent.class, event -> {

                    if (!(event.getEntity() instanceof Player player)) return;

                    // Groupe auquel appartient la nueva instancia (o null si ninguno)
                    Set<Instance> newGroup = InstancesInit.ALL_INSTANCES.stream()
                            .filter(g -> g.contains(event.getInstance()))
                            .findFirst()
                            .orElse(null);

                    Set<Instance> oldGroup = currentGroup.get(player.getUuid());

                    // No hay cambio de grupo ➜ nada que hacer
                    if (Objects.equals(newGroup, oldGroup)) {
                        return;
                    }

                    /* ---------- 1) Salir del grupo antiguo, si lo hay ---------- */
                    if (oldGroup != null) {
                        PlayerData oldData = cache.remove(player.getUuid());
                        if (oldData != null) {
                            repository.save(oldData, oldGroup);
                        }
                    }

                    /* ---------- 2) Entrar en el nuevo grupo, si lo hay ---------- */
                    if (newGroup != null) {
                        PlayerData newData = repository.load(player.getUuid(), newGroup);
                        cache.put(player.getUuid(), newData);

                        // FIX: Call the new applyInventory method
                        applyInventory(player, newData);

                        currentGroup.put(player.getUuid(), newGroup);
                    } else { // Instancia fuera de cualquier grupo seguido
                        currentGroup.remove(player.getUuid());
                    }
                });

        /* ---------------- Desconexión del jugador ---------------- */
        playerNode.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            Set<Instance> group = currentGroup.remove(player.getUuid());
            PlayerData data   = cache.remove(player.getUuid());
            if (data != null && group != null) {
                updateInventory(player, data);
                updatePosition(player, data);
                repository.save(data, group);
            }
        });

        /* ---------------- Modificación del inventario ---------------- */
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

    /* ------------------------------------------------------------------ */
    /* Accès                                                              */
    /* ------------------------------------------------------------------ */

    public PlayerData get(Player player) {
        return cache.get(player.getUuid());
    }

    /**
     * Met à jour et sauvegarde les données du joueur pour son groupe actuel.
     */
    public void savePlayer(Player player) {
        PlayerData data = cache.get(player.getUuid());
        if (data == null) return;
        updateInventory(player, data);
        updatePosition(player, data);
        Set<Instance> group = currentGroup.get(player.getUuid());
        if (group != null) {
            repository.save(data, group);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Tâches planifiées                                                  */
    /* ------------------------------------------------------------------ */

    /** Lance l’auto-save avec l’intervalle par défaut. */
    public void startAutoSave() {
        startAutoSave(DEFAULT_AUTOSAVE_MINUTES);
    }

    /** Lance l’auto-save avec l’intervalle indiqué (en minutes). */
    public void startAutoSave(long minutes) {
        MinecraftServer.getSchedulerManager().buildTask(this::saveAll)
                .delay(TaskSchedule.minutes(minutes))
                .repeat(TaskSchedule.minutes(minutes))
                .schedule();
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::saveAll);
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }

    /** Sauvegarde toutes les données en cache. */
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

    /* ------------------------------------------------------------------ */
    /* Helpers                                                            */
    /* ------------------------------------------------------------------ */

    /** Copie l’inventaire du joueur dans son {@link PlayerData}. */
    private void updateInventory(Player player, PlayerData data) {
        data.inventory.clear();
        PlayerInventory inv = player.getInventory();

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack stack = inv.getItemStack(slot);
            String itemId = ItemUtils.getId(stack);
            // Only save if it's a GameItem
            if (itemId != null) {
                data.inventory.add(new ItemData(itemId, stack.amount(), slot));
            }
        }
    }

    private void updatePosition(Player player, PlayerData data) {
        InstanceContainer instance = (InstanceContainer) player.getInstance();
        if (instance instanceof InstanceContainer container) {
            data.lastInstance = InstancesInit.instance_name_get(container);
            System.out.println(data.lastInstance);
        } else {
            data.lastInstance = null;
        }
        data.position = player.getPosition();
        System.out.println(data.position);
    }

    /**
     * Applies the inventory stored in PlayerData to the actual player's inventory.
     * This method clears the player's current inventory before applying the saved items.
     * @param player The player whose inventory to update.
     * @param data The PlayerData containing the inventory to apply.
     */
    public void applyInventory(Player player, PlayerData data) {
        PlayerInventory inv = player.getInventory();
        inv.clear(); // Clear inventory before loading new items

        for (ItemData item : data.inventory) {
            ItemStack stack = dataToItem(item);
            if (!stack.isAir()) {
                if (item.slot >= 0 && item.slot < inv.getSize()) {
                    inv.setItemStack(item.slot, stack);
                } else {
                    inv.addItemStack(stack); // Fallback for invalid slot, though ideally slots should be valid
                }
            }
        }
    }

    /* ---------- Conversion ItemStack <-> ItemData ---------- */

    private ItemData itemToData(ItemStack stack, int slot) {
        String itemId = ItemUtils.getId(stack);
        // Only return ItemData if it's a GameItem
        if (itemId != null) {
            return new ItemData(itemId, stack.amount(), slot);
        }
        return null; // Do not save vanilla items, so return null
    }

    private ItemStack dataToItem(ItemData data) {
        // If itemId is null, it means it was a vanilla item or empty slot that we chose not to save.
        // In this case, return AIR to represent an empty slot.
        if (data.itemId == null) {
            return ItemStack.AIR;
        }

        GameItem gi = ItemRegistry.byId(data.itemId);
        if (gi != null) {
            return gi.toItemStack().withAmount(data.amount);
        }
        // If itemId is not a GameItem, it means it was an invalid ID. Return AIR.
        return ItemStack.AIR;
    }
}

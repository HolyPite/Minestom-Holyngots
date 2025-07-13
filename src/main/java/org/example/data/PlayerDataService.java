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

                    // Groupe auquel appartient la nouvelle instance (ou null si aucun)
                    Set<Instance> newGroup = InstancesInit.ALL_INSTANCES.stream()
                            .filter(g -> g.contains(event.getInstance()))
                            .findFirst()
                            .orElse(null);

                    Set<Instance> oldGroup = currentGroup.get(player.getUuid());

                    // Pas de changement de groupe ➜ rien à faire
                    if (Objects.equals(newGroup, oldGroup)) return;

                    /* ---------- 1) Quitter l’ancien groupe, le cas échéant ---------- */
                    if (oldGroup != null) {
                        PlayerData oldData = cache.remove(player.getUuid());
                        if (oldData != null) {
                            updateInventory(player, oldData);
                            repository.save(oldData, oldGroup);
                        }
                    }

                    /* ---------- 2) Entrer dans le nouveau groupe, le cas échéant ---------- */
                    if (newGroup != null) {
                        PlayerData newData = repository.load(player.getUuid(), newGroup);
                        cache.put(player.getUuid(), newData);

                        // Appliquer l’inventaire
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
                    } else { // Instance hors de tout groupe suivi
                        currentGroup.remove(player.getUuid());
                    }
                });

        /* ---------------- Déconnexion du joueur ---------------- */
        playerNode.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            Set<Instance> group = currentGroup.remove(player.getUuid());
            PlayerData data   = cache.remove(player.getUuid());
            if (data != null && group != null) {
                updateInventory(player, data);
                repository.save(data, group);
            }
        });

        /* ---------------- Modification de l’inventaire ---------------- */
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
            if (!stack.isAir()) {
                data.inventory.add(itemToData(stack, slot));
            }
        }
    }

    /* ---------- Conversion ItemStack <-> ItemData ---------- */

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
}

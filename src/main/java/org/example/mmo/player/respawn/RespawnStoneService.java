package org.example.mmo.player.respawn;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.bootstrap.InstanceRegistry;
import org.example.data.data_class.PlayerData;
import org.example.mmo.player.data.PlayerDataService;
import org.example.utils.ToastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RespawnStoneService {

    private static final Component UI_TITLE = Component.text("Pierre de respawn");

    private static RespawnStoneRegistry registry;
    private static final List<Entity> stoneDisplays = new ArrayList<>();
    private static final List<Entity> stoneInteractables = new ArrayList<>();
    private static final Map<UUID, RespawnStoneDefinition> interactionLookup = new HashMap<>();

    private RespawnStoneService() {
    }

    public static void init(EventNode<PlayerEvent> playerNode,
                             InstanceRegistry instances,
                             PlayerDataService playerDataService) {
        registry = RespawnStoneBootstrap.init();
        spawnStoneDisplays(instances);

        playerNode.addListener(PlayerEntityInteractEvent.class, event -> {
            RespawnStoneDefinition stone = interactionLookup.get(event.getTarget().getUuid());
            if (stone == null) {
                return;
            }

            Player player = event.getPlayer();
            if (!(player.getInstance() instanceof InstanceContainer container)) {
                return;
            }
            if (!instances.isGameInstance(container)) {
                return;
            }

            openConfirmationInventory(player, container, stone, playerDataService);
        });

        playerNode.addListener(PlayerRespawnEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData data = playerDataService.get(player);
            if (data == null) {
                return;
            }

            InstanceContainer targetInstance = resolveInstance(instances, data);

            Pos respawnPos = data.respawnPosition != null ? data.respawnPosition : player.getRespawnPoint();
            if (respawnPos != null) {
                event.setRespawnPosition(respawnPos);
                player.setRespawnPoint(respawnPos);
            }

            if (targetInstance != null && player.getInstance() != targetInstance) {
                Pos destination = respawnPos != null ? respawnPos : player.getPosition();
                if (destination == null) {
                    destination = Pos.ZERO;
                }
                player.setInstance(targetInstance, destination);
            }
        });
    }

    private static void spawnStoneDisplays(InstanceRegistry instances) {
        stoneDisplays.forEach(Entity::remove);
        stoneInteractables.forEach(Entity::remove);
        stoneDisplays.clear();
        stoneInteractables.clear();
        interactionLookup.clear();

        removeLegacyAnchors(instances);

        for (RespawnStoneDefinition stone : registry.all()) {
            for (Instance instance : instances.gameInstances()) {
                if (!(instance instanceof InstanceContainer container)) {
                    continue;
                }

                Pos displayPos = stone.blockCenter();

                Entity display = new Entity(EntityType.BLOCK_DISPLAY);
                BlockDisplayMeta displayMeta = (BlockDisplayMeta) display.getEntityMeta();
                displayMeta.setBlockState(Block.RESPAWN_ANCHOR);
                displayMeta.setGlowColorOverride(NamedTextColor.GOLD.value());
                display.setNoGravity(true);
                display.setInstance(container, displayPos);
                stoneDisplays.add(display);

                Entity interaction = new Entity(EntityType.INTERACTION);
                InteractionMeta interactionMeta = (InteractionMeta) interaction.getEntityMeta();
                interactionMeta.setWidth(1.2f);
                interactionMeta.setHeight(1.8f);
                interactionMeta.setResponse(true);
                interaction.setBoundingBox(new BoundingBox(1.2, 1.8, 1.2));
                interaction.setNoGravity(true);
                interaction.setInstance(container, displayPos);

                stoneInteractables.add(interaction);
                interactionLookup.put(interaction.getUuid(), stone);
            }
        }
    }

    private static void removeLegacyAnchors(InstanceRegistry instances) {
        for (RespawnStoneDefinition stone : registry.all()) {
            for (Instance instance : instances.gameInstances()) {
                if (!(instance instanceof InstanceContainer container)) {
                    continue;
                }
                if (!container.isChunkLoaded(stone.blockX() >> 4, stone.blockZ() >> 4)) {
                    continue;
                }
                Block current = container.getBlock(stone.blockX(), stone.blockY(), stone.blockZ());
                if (current.compare(Block.RESPAWN_ANCHOR)) {
                    container.setBlock(stone.blockX(), stone.blockY(), stone.blockZ(), Block.AIR);
                }
            }
        }
    }

    private static InstanceContainer resolveInstance(InstanceRegistry instances, PlayerData data) {
        String instanceKey = data != null ? data.respawnInstance : null;
        if (instanceKey == null) {
            return null;
        }
        return instances.byName(instanceKey);
    }

    private static void openConfirmationInventory(Player player,
                                                  InstanceContainer container,
                                                  RespawnStoneDefinition stone,
                                                  PlayerDataService playerDataService) {
        PlayerData data = playerDataService.get(player);
        if (data != null && Objects.equals(data.respawnStoneId, stone.id())) {
            player.sendMessage(Component.text("Vous utilisez déjà cette pierre de respawn.", NamedTextColor.GRAY));
            return;
        }

        Inventory inventory = new Inventory(InventoryType.HOPPER, UI_TITLE);
        inventory.setItemStack(0, ItemStack.of(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItemStack(2, ItemStack.of(Material.BEACON)
                .withCustomName(stone.displayName().color(NamedTextColor.AQUA)));
        inventory.setItemStack(4, ItemStack.of(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItemStack(1, confirmButton(stone));
        inventory.setItemStack(3, cancelButton());

        var eventNode = inventory.eventNode();
        if (eventNode != null) {
            eventNode.addListener(InventoryPreClickEvent.class, clickEvent -> {
                if (clickEvent.getInventory() != inventory) {
                    return;
                }
                clickEvent.setCancelled(true);
                if (clickEvent.getPlayer() != player) {
                    return;
                }
                int slot = clickEvent.getSlot();
                if (slot == 1) {
                    applyStone(player, container, stone, playerDataService);
                    player.closeInventory();
                } else if (slot == 3) {
                    player.closeInventory();
                }
            });
        }

        player.openInventory(inventory);
    }

    private static ItemStack confirmButton(RespawnStoneDefinition stone) {
        Component name = Component.text("Définir ce respawn", NamedTextColor.GREEN);
        Component loreLine = Component.text("-> ", NamedTextColor.GRAY)
                .append(stone.displayName().color(NamedTextColor.GOLD));
        return ItemStack.of(Material.EMERALD_BLOCK)
                .withCustomName(name)
                .withLore(List.of(loreLine));
    }

    private static ItemStack cancelButton() {
        return ItemStack.of(Material.BARRIER)
                .withCustomName(Component.text("Annuler", NamedTextColor.RED));
    }

    private static void applyStone(Player player,
                                   InstanceContainer container,
                                   RespawnStoneDefinition stone,
                                   PlayerDataService playerDataService) {
        boolean updated = playerDataService.updateRespawnPoint(player, container, stone.respawnPosition(), stone.id());
        if (!updated) {
            player.sendMessage(Component.text("Impossible d'enregistrer ce point de respawn.", NamedTextColor.RED));
            return;
        }

        playerDataService.savePlayer(player);
        ToastManager.showToast(player, Component.text("Respawn lié à ").append(stone.displayName()),
                Material.RESPAWN_ANCHOR, FrameType.TASK);
        player.playSound(Sound.sound(Key.key("minecraft:block.respawn_anchor.charge"), Sound.Source.MASTER, 1f, 1f),
                stone.blockCenter());
    }
}

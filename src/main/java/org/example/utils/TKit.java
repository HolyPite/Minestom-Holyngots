package org.example.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.color.DyeColor;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.mmo.item.ItemUtils;

import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(TKit.class);

    public static void playSound(Instance inst, Pos Pos,
                                 String soundName, Sound.Source source,         // Sound.Source.MASTER …
                                 float volume, float pitch) {
        if (inst == null) {
            return;
        }

        inst.playSound(Sound.sound(Key.key(soundName), source, volume, pitch), Pos.x(), Pos.y(), Pos.z());
    }

    public static void spawnParticles(Instance inst, Particle particle, Pos Pos,
                                      float offsetX, float offsetY, float offsetZ,
                                      float maxSpeed, int number) {
        if (inst == null) {
            return;
        }

        inst.sendGroupedPacket(new ParticlePacket(particle, Pos.x(), Pos.y(), Pos.z(), offsetX, offsetY, offsetZ, maxSpeed, number));
    }

    public static void drop(Instance instance, ItemStack item, Pos pos) {
        ItemEntity itemEntity = new ItemEntity(item);
        itemEntity.setPickupDelay(Duration.ofMillis(500));
        itemEntity.setInstance(instance, pos);
    }

    // Extrait le texte brut d’un component Adventure (récursif)
    public static String extractPlainText(Component component) {
        StringBuilder plainText = new StringBuilder();
        if (component instanceof net.kyori.adventure.text.TextComponent textComponent) {
            plainText.append(textComponent.content());
        }
        for (Component child : component.children()) {
            plainText.append(extractPlainText(child));
        }
        return plainText.toString();
    }

    // Texte avec dégradé de couleur (Adventure)
    public static Component createGradientText(String text, TextColor startColor, TextColor endColor) {
        int length = text.length();
        Component gradientText = Component.empty();
        for (int i = 0; i < length; i++) {

            float ratio = (float) i / Math.max(1, length - 1);

            int red = interpolate(startColor.red(), endColor.red(), ratio);
            int green = interpolate(startColor.green(), endColor.green(), ratio);
            int blue = interpolate(startColor.blue(), endColor.blue(), ratio);

            TextColor color = TextColor.color(red, green, blue);

            gradientText = gradientText.append(Component.text(String.valueOf(text.charAt(i))).color(color));
        }

        return gradientText;
    }

    private static int interpolate(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }

    // Distance² (utile pour comparaisons rapides)
    public static double distanceSquared(Pos a, Pos b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    // Calcul de la distance euclidienne entre deux Pos
    public static double distance(Pos a, Pos b) {
        return Math.sqrt(distanceSquared(a,b));
    }

    // Envoie un message coloré à un joueur
    public static void sendStyledMessage(Player player, String message, TextColor color) {
        player.sendMessage(Component.text(message).color(color));
    }

    // Retourne une couleur de laine random
    public static DyeColor getRandomDyeColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    // Retourne true avec un certain pourcentage de chance (entre 0 et 1)
    public static boolean chance(double chance) {
        if (chance < 0.0 || chance > 1.0)
            throw new IllegalArgumentException("Chance doit être entre 0 et 1");
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    // Formate un temps en "MM:SS"
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    // Retourne la direction entre deux Pos, normalisée (utilisable pour propulser un entity)
    public static Vec getDirection(Pos from, Pos to) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0) return new Vec(0, 0, 0);
        Vec dir = new Vec(dx, dy, dz);
        return dir.normalize();
    }

    // Retourne la liste des entités dans un rayon donné
    public static List<Entity> getEntitiesInRadius(Instance instance, Pos center, double radius) {
        List<Entity> entities = new ArrayList<>();
        double radiusSq = radius * radius;
        for (Entity entity : instance.getEntities()) {
            if (distanceSquared(center, entity.getPosition()) <= radiusSq) {
                entities.add(entity);
            }
        }
        return entities;
    }

    public static List<LivingEntity> getLivingEntitiesInRadius(Instance instance,
                                                               Pos center,
                                                               double radius) {
        double radiusSq = radius * radius;
        List<LivingEntity> living = new ArrayList<>();

        for (Entity e : instance.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;          // on ignore ce qui n’est pas vivant
            if (distanceSquared(center, le.getPosition()) <= radiusSq)
                living.add(le);
        }
        return living;
    }

    public static List<Player> getPlayersInRadius(Instance instance, Pos center, double radius) {
        List<Player> players = new ArrayList<>();
        double radiusSq = radius * radius;
        for (Entity entity : instance.getEntities()) {
            if (entity instanceof Player player) {
                if (distanceSquared(center, player.getPosition()) <= radiusSq) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    // Retourne le joueur le plus proche d’une position donnée (null si aucun)
    public static Player getNearestPlayer(Instance instance, Pos pos) {
        Player nearest = null;
        double minDistSq = Double.MAX_VALUE;
        for (Player p : instance.getPlayers()) {
            double distSq = distanceSquared(pos, p.getPosition());
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }

    // Drop une série d’items en cercle autour d’une position
    public static void dropItemsInCircle(Instance instance, Pos center, ItemStack[] items, double radius) {
        int angleStep = 360 / items.length;
        for (int i = 0; i < items.length; i++) {
            double angle = Math.toRadians(i * angleStep);
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            Pos dropPos = new Pos(x, center.y(), z);
            drop(instance, items[i], dropPos);

        }
    }

    public static BlockWithPosition blockAbove(Instance inst, BlockWithPosition b) {
        int x = b.x(), y = b.y() + 1, z = b.z();
        return new BlockWithPosition(inst.getBlock(x, y, z), x, y, z);
    }

    public static BlockWithPosition blockAbove(Instance inst, BlockWithPosition b, int plus) {
        int x = b.x(), y = b.y() + plus, z = b.z();
        return new BlockWithPosition(inst.getBlock(x, y, z), x, y, z);
    }

    // Retourne la liste des blocs dans un cube autour d’une position
    public static List<Block> getBlocksInCube(Instance instance, Pos center, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = instance.getBlock(
                            (int) Math.floor(center.x()) + x,
                            (int) Math.floor(center.y()) + y,
                            (int) Math.floor(center.z()) + z
                    );
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public static List<BlockWithPosition> getBlocksInSphere(Instance instance, Pos center, double radius) {
        List<BlockWithPosition> result = new ArrayList<>();
        int baseX = (int) Math.floor(center.x());
        int baseY = (int) Math.floor(center.y());
        int baseZ = (int) Math.floor(center.z());
        double radiusSq = radius * radius;
        for (int x = -((int)Math.ceil(radius)); x <= ((int)Math.ceil(radius)); x++) {
            for (int y = -((int)Math.ceil(radius)); y <= ((int)Math.ceil(radius)); y++) {
                for (int z = -((int)Math.ceil(radius)); z <= ((int)Math.ceil(radius)); z++) {
                    if (x * x + y * y + z * z <= radiusSq) {
                        int bx = baseX + x;
                        int by = baseY + y;
                        int bz = baseZ + z;
                        Block block = instance.getBlock(bx, by, bz);
                        result.add(new BlockWithPosition(block, bx, by, bz));
                    }
                }
            }
        }
        return result;
    }


    // Teste si tous les blocs d'une liste sont de l'air
    public static boolean areAllBlocksAir(List<Block> blocks) {
        for (Block block : blocks) {
            if (!block.isAir()) return false;
        }
        return true;
    }

    // Renvoie un bloc solide sous une position (si trouvé, sinon null)
    public static Block getBlockUnder(Instance instance, Pos pos) {
        int x = (int) Math.floor(pos.x());
        int y = (int) Math.floor(pos.y()) - 1;
        int z = (int) Math.floor(pos.z());
        Block block = instance.getBlock(x, y, z);
        return block.isAir() ? null : block;
    }

    // Envoie un message au joueur le plus proche d’une position
    public static void messageNearestPlayer(Instance instance, Pos pos, String message) {
        Player player = getNearestPlayer(instance, pos);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    // Donne des items (dans les slots disponibles, drop au sol si inventaire plein)
    public static void giveItems(Player player, ItemStack... items) {
        for (ItemStack item : items) {
            boolean added = player.getInventory().addItemStack(item);
            if (!added) {
                // Drop l’item au sol si inventaire plein
                drop(player.getInstance(),item,player.getPosition());
            }
        }
    }

    /**
     * Checks if a player has a sufficient amount of a specific item.
     * @param player The player to check.
     * @param reference The item to look for (material and NBT must match).
     * @param amount The required amount.
     * @return true if the player has at least the specified amount.
     */
    public static boolean hasItems(Player player, ItemStack reference, int amount) {
        if (amount <= 0 || reference.isAir()) return true;

        var inv = player.getInventory();
        int available = 0;
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack stack = inv.getItemStack(slot);
            if (stack.isAir()) continue;

            boolean same = (stack.material() == reference.material() && Objects.equals(ItemUtils.getId(stack), ItemUtils.getId(reference)));
            if (same) available += stack.amount();
        }
        return available >= amount;
    }

    /**
     * Removes a list of items from a player's inventory transactionally.
     * All items are removed only if the player possesses all of them.
     * @param player The player to remove items from.
     * @param itemsToRemove The list of ItemStacks to remove.
     * @return true if all items were successfully removed, false otherwise.
     */
    public static boolean removeItemsList(Player player, List<ItemStack> itemsToRemove) {
        if (itemsToRemove == null || itemsToRemove.isEmpty()) {
            return true;
        }

        // --- 1. Verification Pass ---
        Map<ItemStack, Integer> requiredAmounts = new HashMap<>();
        for (ItemStack item : itemsToRemove) {
            if (item.isAir()) continue;
            ItemStack keyItem = item.withAmount(1);
            requiredAmounts.merge(keyItem, item.amount(), Integer::sum);
        }

        for (Map.Entry<ItemStack, Integer> entry : requiredAmounts.entrySet()) {
            if (!hasItems(player, entry.getKey(), entry.getValue())) {
                return false; // Abort if any item is missing
            }
        }

        // --- 2. Removal Pass ---
        for (Map.Entry<ItemStack, Integer> entry : requiredAmounts.entrySet()) {
            removeItems(player, entry.getKey(), entry.getValue());
        }

        return true;
    }

    public static boolean removeItems(Player player, ItemStack reference, int amount) {
        if (amount <= 0 || reference.isAir()) return true;          // rien à retirer

        if (!hasItems(player, reference, amount)) {
            return false; // Not enough items, do nothing
        }
        LOGGER.debug("Sufficient items found for removal");

        var inv = player.getInventory();
        int amountToRemove = amount;
        for (int slot = 0; slot < inv.getSize() && amountToRemove > 0; slot++) {
            ItemStack stack = inv.getItemStack(slot);
            if (stack.isAir()) continue;

            boolean same =(stack.material() == reference.material() && Objects.equals(ItemUtils.getId(stack), ItemUtils.getId(reference)));
            if (!same) continue;

            int take = Math.min(amountToRemove, stack.amount());
            amountToRemove -= take;

            if (stack.amount() == take) {
                inv.setItemStack(slot, ItemStack.AIR);                  // vide la case
            } else {
                inv.setItemStack(slot, stack.withAmount(stack.amount() - take));
            }
        }
        return true;    // retrait effectué avec succès
    }

    public static boolean removeOneItem(Player player) {
        int slot = player.getHeldSlot();
        ItemStack inHand = player.getInventory().getItemStack(slot);
        return removeItems(player, inHand, 1);
    }


    public static List<BlockWithPosition> destroySphere(InstanceContainer inst,
                                                        Pos centre,
                                                        double rayon,
                                                        double probabilité) {
        List<BlockWithPosition> sphere = getBlocksInSphere(inst, centre, rayon);
        List<BlockWithPosition> détruits = new ArrayList<>();

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (BlockWithPosition b : sphere) {
            if (b.block().isAir()) continue;
            if (rnd.nextDouble() > probabilité) continue;

            inst.setBlock(b.x(), b.y(), b.z(), Block.AIR);
            détruits.add(b);
        }
        return détruits;
    }


    public static void applyEffects(LivingEntity target,
                                    PotionEffect[] effects,
                                    short[] ticks,
                                    byte[] ampl) {
        if (effects.length != ticks.length || effects.length != ampl.length){
            throw new IllegalArgumentException("Tableaux de tailles différentes");
        }

        for (int i = 0; i < effects.length; i++) {
            target.addEffect(new Potion(effects[i], ampl[i], ticks[i]));
        }
    }

    /**
     * Émule un AreaEffectCloud :
     *  – Particules circulaires
     *  – Applique les potions une seule fois à l’entrée dans la zone
     */
    public static void spawnFakeEffectCloud(InstanceContainer inst,
                                            Pos center,
                                            float radius,
                                            int lifetimeTicks,
                                            Particle particle,
                                            PotionEffect[] types,
                                            short[] dur,
                                            byte[] amp) {

        /* tâche répétée toutes les 2 ticks ~0,1 s */
        Task t =  inst.scheduler().buildTask(() -> {

                    // 1) visuel cercle de particules
                    spawnParticles(inst, particle, center, radius/2, 0.2f, radius/2, 0, (int) (20*radius*radius));

                    // 2) application effets
                    getLivingEntitiesInRadius(inst, center, radius).forEach((le) -> {
                        applyEffects(le, types, dur, amp);
                    });

                }).repeat(TaskSchedule.tick(2))
                .schedule();

        /* arrêt après lifetimeTicks */
        inst.scheduler()
                .buildTask(t::cancel)
                .delay(TaskSchedule.tick(lifetimeTicks))
                .schedule();
    }


    public static int countItems(Player player, ItemStack reference) {
        String referenceId = ItemUtils.getId(reference);
        if (referenceId == null) { // It's a vanilla item
            int amount = 0;
            for (ItemStack stack : player.getInventory().getItemStacks()) {
                if (stack.material() == reference.material()) {
                    amount += stack.amount();
                }
            }
            return amount;
        } else { // It's a GameItem
            int amount = 0;
            for (ItemStack stack : player.getInventory().getItemStacks()) {
                if (Objects.equals(ItemUtils.getId(stack), referenceId)) {
                    amount += stack.amount();
                }
            }
            return amount;
        }
    }


    /*
    public static void spawnEffectCloud(InstanceContainer inst,
                                        Pos center,
                                        float radius,
                                        int lifetimeTicks,
                                        Particle particle,
                                        PotionEffect[] effects,
                                        short[] ticks,
                                        byte[] ampl) {

        EntityCreature cloud = new EntityCreature(EntityType.AREA_EFFECT_CLOUD);
        cloud.setInstance(inst, center);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) cloud.getEntityMeta();

        //meta.setRadius(radius);
        //meta.setParticle(particle);
        //meta.setColor(1);
        //meta.setSinglePoint(true);

        cloud.scheduler().buildTask(() -> {

                    if (cloud.isRemoved()) return;

                    List<LivingEntity> victims = getLivingEntitiesInRadius(inst, cloud.getPosition(),radius);

                    for (LivingEntity v : victims) {            // premier passage ⇒ on applique
                        applyEffects(v, effects, ticks, ampl);
                    }

                }).repeat(TaskSchedule.tick(2))              // toutes les 2 ticks (~0,1 s)
                .schedule();

        inst.scheduler().buildTask(cloud::remove).delay(TaskSchedule.tick(lifetimeTicks)).schedule();
    }
    */




}

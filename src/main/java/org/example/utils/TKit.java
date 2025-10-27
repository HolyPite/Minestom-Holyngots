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
import org.example.mmo.item.ItemUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class TKit {

    public static void playSound(Instance inst, Pos Pos,
                                 String soundName, Sound.Source source,         // Sound.Source.MASTER …
                                 float volume, float pitch) {

        inst.playSound(Sound.sound(Key.key(soundName), source, volume, pitch), Pos.x(), Pos.y(), Pos.z());
    }

    public static void spawnParticles(Instance inst, Particle particle, Pos Pos,
                                      float offsetX, float offsetY, float offsetZ,
                                      float maxSpeed, int number) {

        inst.sendGroupedPacket(new ParticlePacket(particle, Pos.x(), Pos.y(), Pos.z(), offsetX, offsetY, offsetZ, maxSpeed, number));
    }

    public static void drop(Instance instance, ItemStack item, Pos pos) {
        ItemEntity itemEntity = new ItemEntity(item);
        itemEntity.setPickupDelay(Duration.ofMillis(500));
        itemEntity.setInstance(instance, pos);
    }

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

    public static double distanceSquared(Pos a, Pos b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public static double distance(Pos a, Pos b) {
        return Math.sqrt(distanceSquared(a,b));
    }

    public static void sendStyledMessage(Player player, String message, TextColor color) {
        player.sendMessage(Component.text(message).color(color));
    }

    public static DyeColor getRandomDyeColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    public static boolean chance(double chance) {
        if (chance < 0.0 || chance > 1.0)
            throw new IllegalArgumentException("Chance doit être entre 0 et 1");
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public static Vec getDirection(Pos from, Pos to) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0) return new Vec(0, 0, 0);
        Vec dir = new Vec(dx, dy, dz);
        return dir.normalize();
    }

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
            if (!(e instanceof LivingEntity le)) continue;
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

    public static void giveItems(Player player, ItemStack... items) {
        for (ItemStack item : items) {
            boolean added = player.getInventory().addItemStack(item);
            if (!added) {
                drop(player.getInstance(),item,player.getPosition());
            }
        }
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

    public static boolean hasItems(Player player, ItemStack reference, int amount) {
        return countItems(player, reference) >= amount;
    }

    public static boolean removeItems(Player player, ItemStack reference, int amount) {
        if (amount <= 0) return true;
        if (!hasItems(player, reference, amount)) return false;

        String referenceId = ItemUtils.getId(reference);
        int amountToRemove = amount;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            if (stack.isAir()) continue;

            boolean isTargetItem;
            if (referenceId == null) {
                isTargetItem = stack.material() == reference.material();
            } else {
                isTargetItem = Objects.equals(ItemUtils.getId(stack), referenceId);
            }

            if (isTargetItem) {
                int take = Math.min(amountToRemove, stack.amount());
                player.getInventory().setItemStack(i, stack.withAmount(stack.amount() - take));
                amountToRemove -= take;
                if (amountToRemove <= 0) {
                    return true;
                }
            }
        }
        return true;
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
                        result.add(new BlockWithPosition(instance.getBlock(bx, by, bz), bx, by, bz));
                    }
                }
            }
        }
        return result;
    }
}

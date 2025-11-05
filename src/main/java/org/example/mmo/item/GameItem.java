package org.example.mmo.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.CustomModelData;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatMap;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.projectile.ProjectileLaunchConfig;
import org.example.utils.TKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class GameItem {

    public final String id;
    public final Component displayName;
    public final Rarity rarity;
    public final Category category;
    public final boolean tradable;
    public final String customModel;
    public final Material material;
    public final StatMap stats;
    public final List<String> story;
    public final int maxStack;
    public final boolean questItem;
    public final ProjectileOptions projectileOptions;

    private GameItem(Builder b) {
        this.id = b.id;
        this.displayName = b.displayName;
        this.rarity = b.rarity;
        this.category = b.category;
        this.tradable = b.tradable;
        this.customModel = id;
        this.material = b.material;
        this.stats = b.stats;
        this.story = List.copyOf(b.story);
        this.maxStack = b.maxStack;
        this.questItem = b.questItem;
        this.projectileOptions = b.projectileOptions;
    }

    public String getId() {
        return id;
    }

    public ItemStack toItemStack() {
        return ItemStack.of(material)
                .with(DataComponents.CUSTOM_NAME, displayName)
                .with(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(customModel), List.of()))
                .with(DataComponents.LORE, buildLore())
                .with(DataComponents.MAX_STACK_SIZE, maxStack)
                .with(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(List.of()))
                .withoutExtraTooltip();
    }

    private List<Component> buildLore() {
        List<Component> lore = new ArrayList<>();

        if (!story.isEmpty()) {
            story.forEach(line -> lore.add(Component.text(line, NamedTextColor.GRAY, TextDecoration.ITALIC)));
            lore.add(Component.empty());
        }

        lore.add(Component.text(rarity.name(), rarity.color, TextDecoration.BOLD));
        lore.add(Component.text(category.name(), NamedTextColor.GRAY));
        lore.add(Component.empty());

        stats.forEach((stat, value) -> {
            Component line = Component.text(" - ", NamedTextColor.GRAY);
            line = line.append(Component.text(stat.label + " :", NamedTextColor.GRAY).decorate(TextDecoration.UNDERLINED));

            String valueStr = switch (stat.kind) {
                case FLAT -> " %s%d".formatted(value > 0 ? "+" : "", value);
                case PROBA -> " %d%%".formatted(value);
                case PERCENT -> " %s%d%%".formatted(value > 0 ? "+" : "", value);
            };

            NamedTextColor color = (value >= 0) ? NamedTextColor.WHITE : NamedTextColor.RED;
            line = line.append(Component.text(valueStr, color));
            lore.add(line);
        });

        if (projectileOptions != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Projectile", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
            lore.add(Component.text(" - Type : " + projectileOptions.projectileType().name(), NamedTextColor.GRAY));
            lore.add(Component.text(" - Cadence : " + projectileOptions.cooldownTicks() + " ticks", NamedTextColor.GRAY));
        }

        if (questItem) {
            lore.add(Component.empty());
            lore.add(Component.text("Objet de quete", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
        }

        if (!tradable) {
            lore.add(Component.empty());
            lore.add(TKit.createGradientText("Ne peut pas etre echange",
                    TextColor.color(0xFF5151),
                    TextColor.color(0xFF9EA3))
                    .decorate(TextDecoration.ITALIC));
        }
        return lore;
    }

    public Optional<ProjectileOptions> projectileOptions() {
        return Optional.ofNullable(projectileOptions);
    }

    public boolean isProjectileLauncher() {
        return projectileOptions != null;
    }

    public static final class Builder {
        private final String id;
        private final Component displayName;
        private final String customModel;

        private Rarity rarity = Rarity.COMMON;
        private Category category = Category.MISC;
        private boolean tradable = true;
        private Material material = Material.STICK;
        private final StatMap stats = new StatMap();
        private List<String> story = Collections.emptyList();
        private int maxStack = 64;
        private boolean questItem = false;
        private ProjectileOptions projectileOptions;

        public Builder(String id, Component name) {
            this.id = id;
            this.displayName = name;
            this.customModel = id;
        }

        public Builder rarity(Rarity r) {
            this.rarity = r;
            return this;
        }

        public Builder category(Category c) {
            this.category = c;
            return this;
        }

        public Builder tradable(boolean t) {
            this.tradable = t;
            return this;
        }

        public Builder material(Material m) {
            this.material = m;
            return this;
        }

        public Builder stat(StatType t, int v) {
            this.stats.with(t, v);
            return this;
        }

        public Builder story(String... lines) {
            this.story = Arrays.asList(lines);
            return this;
        }

        public Builder story(List<String> lines) {
            this.story = List.copyOf(lines);
            return this;
        }

        public Builder stackSize(int n) {
            this.maxStack = Math.max(1, Math.min(n, 64));
            return this;
        }

        public Builder questItem(boolean isQuestItem) {
            this.questItem = isQuestItem;
            return this;
        }

        public Builder projectile(Consumer<ProjectileOptions.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            ProjectileOptions.Builder builder = new ProjectileOptions.Builder();
            consumer.accept(builder);
            this.projectileOptions = builder.build();
            return this;
        }

        public Builder clearProjectile() {
            this.projectileOptions = null;
            return this;
        }

        public GameItem build() {
            return new GameItem(this);
        }
    }

    public static final class ProjectileOptions {

        public enum Trigger {
            LEFT_CLICK,
            RIGHT_CLICK,
            BOTH;

            boolean accepts(boolean left) {
                return this == BOTH || (left && this == LEFT_CLICK) || (!left && this == RIGHT_CLICK);
            }
        }

        private final Trigger trigger;
        private final EntityType projectileType;
        private final double speed;
        private final double spread;
        private final boolean hasGravity;
        private final long lifetimeTicks;
        private final Long blockLifetimeTicks;
        private final double range;
        private final long cooldownTicks;
        private final boolean allowOffHand;

        private ProjectileOptions(Trigger trigger,
                                  EntityType projectileType,
                                  double speed,
                                  double spread,
                                  boolean hasGravity,
                                  long lifetimeTicks,
                                  Long blockLifetimeTicks,
                                  double range,
                                  long cooldownTicks,
                                  boolean allowOffHand) {
            this.trigger = trigger;
            this.projectileType = projectileType;
            this.speed = speed;
            this.spread = spread;
            this.hasGravity = hasGravity;
            this.lifetimeTicks = lifetimeTicks;
            this.blockLifetimeTicks = blockLifetimeTicks;
            this.range = range;
            this.cooldownTicks = cooldownTicks;
            this.allowOffHand = allowOffHand;
        }

        public Trigger trigger() {
            return trigger;
        }

        public EntityType projectileType() {
            return projectileType;
        }

        public double speed() {
            return speed;
        }

        public double spread() {
            return spread;
        }

        public boolean hasGravity() {
            return hasGravity;
        }

        public long lifetimeTicks() {
            return lifetimeTicks;
        }

        public Optional<Long> blockLifetimeTicks() {
            return Optional.ofNullable(blockLifetimeTicks);
        }

        public double range() {
            return range;
        }

        public long cooldownTicks() {
            return cooldownTicks;
        }

        public boolean allowOffHand() {
            return allowOffHand;
        }

        public ProjectileLaunchConfig toLaunchConfig() {
            ProjectileLaunchConfig.Builder builder = ProjectileLaunchConfig.builder(projectileType)
                    .speed(speed)
                    .spread(spread)
                    .hasGravity(hasGravity)
                    .lifetimeTicks(lifetimeTicks);
            if (blockLifetimeTicks != null) {
                builder.blockLifetimeTicks(blockLifetimeTicks);
            }
            return builder.build();
        }

        boolean acceptsTrigger(boolean leftClick) {
            return trigger.accepts(leftClick);
        }

        public static final class Builder {
            private Trigger trigger = Trigger.RIGHT_CLICK;
            private EntityType projectileType;
            private double speed = 1.2D;
            private double spread = 0.0D;
            private boolean hasGravity = true;
            private long lifetimeTicks = ProjectileLaunchConfig.DEFAULT_LIFETIME_TICKS;
            private Long blockLifetimeTicks;
            private double range = 24.0D;
            private long cooldownTicks = 10L;
            private boolean allowOffHand = false;

            public Builder trigger(Trigger trigger) {
                this.trigger = Objects.requireNonNull(trigger, "trigger");
                return this;
            }

            public Builder projectileType(EntityType projectileType) {
                this.projectileType = Objects.requireNonNull(projectileType, "projectileType");
                return this;
            }

            public Builder speed(double speed) {
                this.speed = speed;
                return this;
            }

            public Builder spread(double spread) {
                this.spread = spread;
                return this;
            }

            public Builder hasGravity(boolean hasGravity) {
                this.hasGravity = hasGravity;
                return this;
            }

            public Builder lifetimeTicks(long lifetimeTicks) {
                this.lifetimeTicks = lifetimeTicks;
                return this;
            }

            public Builder blockLifetimeTicks(long blockLifetimeTicks) {
                this.blockLifetimeTicks = blockLifetimeTicks;
                return this;
            }

            public Builder range(double range) {
                this.range = range;
                return this;
            }

            public Builder cooldownTicks(long cooldownTicks) {
                this.cooldownTicks = cooldownTicks;
                return this;
            }

            public Builder allowOffHand(boolean allowOffHand) {
                this.allowOffHand = allowOffHand;
                return this;
            }

            public ProjectileOptions build() {
                if (projectileType == null) {
                    throw new IllegalStateException("projectileType must be specified for projectile launcher items");
                }
                double resolvedSpeed = speed > 0 ? speed : 1.0D;
                double resolvedRange = range > 0 ? range : 24.0D;
                double resolvedSpread = Math.max(0.0D, spread);
                long resolvedCooldown = Math.max(0L, cooldownTicks);
                return new ProjectileOptions(
                        trigger,
                        projectileType,
                        resolvedSpeed,
                        resolvedSpread,
                        hasGravity,
                        lifetimeTicks,
                        blockLifetimeTicks,
                        resolvedRange,
                        resolvedCooldown,
                        allowOffHand
                );
            }
        }
    }
}

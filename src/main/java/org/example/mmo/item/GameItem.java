package org.example.mmo.item;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
import org.example.mmo.item.datas.AmmoType;
import org.example.mmo.item.datas.Category;
import org.example.mmo.item.datas.Rarity;
import org.example.mmo.item.datas.StatMap;
import org.example.mmo.item.datas.StatType;
import org.example.mmo.item.skill.SkillDefinition;
import org.example.mmo.item.skill.SkillInstance;
import org.example.mmo.item.skill.SkillLibrary;
import org.example.mmo.item.skill.SkillTrigger;
import org.example.mmo.projectile.ProjectileLaunchConfig;
import org.example.utils.TKit;

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
    public final AmmoOptions ammoOptions;
    private final List<SkillInstance> skills;

    private GameItem(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.rarity = builder.rarity;
        this.category = builder.category;
        this.tradable = builder.tradable;
        this.customModel = id;
        this.material = builder.material;
        this.stats = builder.stats;
        this.story = List.copyOf(builder.story);
        this.maxStack = builder.maxStack;
        this.questItem = builder.questItem;
        this.projectileOptions = builder.projectileOptions;
        this.ammoOptions = builder.ammoOptions;
        this.skills = buildSkills(builder.skills);
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

            List<String> primaryStats = new ArrayList<>();
            primaryStats.add("Cadence : " + projectileOptions.cooldownTicks() + " ticks");
            primaryStats.add("Déclencheur : " + projectileOptions.trigger().name());
            lore.add(composeInlineStatLine(primaryStats));

            List<String> mechanicStats = new ArrayList<>();
            mechanicStats.add("Vitesse : " + formatDecimal(projectileOptions.speed()));
            if (projectileOptions.spread() > 0.0D) {
                mechanicStats.add("Dispersion : " + formatDecimal(projectileOptions.spread()));
            }
            mechanicStats.add("Gravite : " + (projectileOptions.hasGravity() ? "Oui" : "Non"));
            lore.add(composeInlineStatLine(mechanicStats));

            List<String> extraStats = new ArrayList<>();
            if (projectileOptions.chargeTicks() > 0) {
                extraStats.add("Charge : " + projectileOptions.chargeTicks() + " ticks");
            }
            if (projectileOptions.consumesAmmo()) {
                extraStats.add("Munition : " + projectileOptions.ammoType().name() + " x" + projectileOptions.ammoPerShot());
            }
            if (!extraStats.isEmpty()) {
                lore.add(composeInlineStatLine(extraStats));
            }
        }

        if (ammoOptions != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Munition", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            lore.add(Component.text(" - Type : " + ammoOptions.type().name(), NamedTextColor.GRAY));
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
        appendSkillsLore(lore);
        return lore;
    }

    private static Component composeInlineStatLine(List<String> entries) {
        if (entries.isEmpty()) {
            return Component.empty();
        }
        Component line = Component.text(" - ", NamedTextColor.GRAY);
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                line = line.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
            }
            line = line.append(Component.text(entries.get(i), NamedTextColor.GRAY));
        }
        return line;
    }

    private static String formatDecimal(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-6) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    public Optional<ProjectileOptions> projectileOptions() {
        return Optional.ofNullable(projectileOptions);
    }

    public Optional<AmmoOptions> ammoOptions() {
        return Optional.ofNullable(ammoOptions);
    }

    public boolean isProjectileLauncher() {
        return projectileOptions != null;
    }

    public boolean isAmmo() {
        return ammoOptions != null;
    }

    private static List<SkillInstance> buildSkills(List<SkillDefinition> definitions) {
        if (definitions.isEmpty()) {
            return List.of();
        }
        List<SkillInstance> resolved = new ArrayList<>();
        for (SkillDefinition definition : definitions) {
            SkillLibrary.resolve(definition).ifPresent(resolved::add);
        }
        return List.copyOf(resolved);
    }

    public List<SkillInstance> skills() {
        return skills;
    }

    public boolean hasSkills() {
        return !skills.isEmpty();
    }

    private void appendSkillsLore(List<Component> lore) {
        if (skills.isEmpty()) {
            return;
        }
        lore.add(Component.empty());
        lore.add(Component.text("Pouvoirs", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        for (SkillInstance skill : skills) {
            lore.add(buildSkillLine(skill));
        }
    }

    private Component buildSkillLine(SkillInstance skill) {
        SkillDefinition def = skill.definition();
        String name = prettifyPowerId(def.powerId());
        String triggers = formatTriggerList(def.triggers());
        String cooldown = formatCooldown(def.cooldown());
        String level = def.level() > 1 ? "Niv. " + def.level() : null;
        String params = formatParameters(def.resolveParameters().asMap());

        Component line = Component.text(" - ", NamedTextColor.GRAY)
                .append(Component.text(name, NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
        if (level != null) {
            line = line.append(Component.text(" (" + level + ")", NamedTextColor.GRAY));
        }
        line = line.append(Component.text(" • " + triggers, NamedTextColor.DARK_GRAY));
        if (cooldown != null) {
            line = line.append(Component.text(" | " + cooldown, NamedTextColor.GRAY));
        }
        if (params != null && !params.isBlank()) {
            line = line.append(Component.text(" | " + params, NamedTextColor.DARK_GRAY));
        }
        return line;
    }

    private static String formatTriggerList(Set<SkillTrigger> triggers) {
        if (triggers.isEmpty()) {
            return "Passif";
        }
        List<String> labels = new ArrayList<>();
        for (SkillTrigger trigger : triggers) {
            labels.add(triggerLabel(trigger));
        }
        return String.join(", ", labels);
    }

    private static String triggerLabel(SkillTrigger trigger) {
        return switch (trigger) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> "Clic droit";
            case LEFT_CLICK_AIR, LEFT_CLICK_ENTITY -> "Clic gauche";
            case INVENTORY_CLICK -> "Inventaire";
            case INVENTORY_CHANGE -> "Changement equip.";
            case HELD_TICK, ENTITY_TICK -> "Maintien";
            case ENTITY_AGGRO -> "Aggro";
            case ENTITY_DAMAGED -> "Dommage subi";
            case ENTITY_DEATH -> "A la mort";
            case ENTITY_SPAWN -> "Apparition";
            default -> trigger.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        };
    }

    private static String formatCooldown(Duration cooldown) {
        if (cooldown == null || cooldown.isZero()) {
            return null;
        }
        double seconds = cooldown.toMillis() / 1000.0;
        return "CD " + (seconds % 1 == 0 ? (int) seconds + "s" : String.format(Locale.US, "%.1fs", seconds));
    }

    private static String formatParameters(Map<String, Double> params) {
        if (params.isEmpty()) {
            return null;
        }
        List<String> chunks = new ArrayList<>();
        params.forEach((key, value) -> chunks.add(key + "=" + formatParamValue(value)));
        return String.join(", ", chunks);
    }

    private static String formatParamValue(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-6) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static String prettifyPowerId(String id) {
        if (id == null || id.isBlank()) {
            return "Pouvoir";
        }
        int colon = id.indexOf(':');
        String tail = colon >= 0 ? id.substring(colon + 1) : id;
        tail = tail.replace('_', ' ');
        return tail.isEmpty() ? "Pouvoir" : Character.toUpperCase(tail.charAt(0)) + tail.substring(1);
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
        private AmmoOptions ammoOptions;
        private final List<SkillDefinition> skills = new ArrayList<>();

        public Builder(String id, Component name) {
            this.id = id;
            this.displayName = name;
            this.customModel = id;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = Objects.requireNonNull(rarity, "rarity");
            return this;
        }

        public Builder category(Category category) {
            this.category = Objects.requireNonNull(category, "category");
            return this;
        }

        public Builder tradable(boolean tradable) {
            this.tradable = tradable;
            return this;
        }

        public Builder material(Material material) {
            this.material = Objects.requireNonNull(material, "material");
            return this;
        }

        public Builder stat(StatType type, int value) {
            this.stats.with(type, value);
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

        public Builder stackSize(int maxStack) {
            this.maxStack = Math.max(1, Math.min(maxStack, 64));
            return this;
        }

        public Builder questItem(boolean questItem) {
            this.questItem = questItem;
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

        public Builder ammo(Consumer<AmmoOptions.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            AmmoOptions.Builder builder = new AmmoOptions.Builder();
            consumer.accept(builder);
            this.ammoOptions = builder.build();
            return this;
        }

        public Builder clearAmmo() {
            this.ammoOptions = null;
            return this;
        }

        public Builder skill(SkillDefinition definition) {
            Objects.requireNonNull(definition, "definition");
            this.skills.add(definition);
            return this;
        }

        public Builder skill(String powerId, Consumer<SkillDefinition.Builder> consumer) {
            Objects.requireNonNull(powerId, "powerId");
            Objects.requireNonNull(consumer, "consumer");
            SkillDefinition.Builder skillBuilder = SkillDefinition.builder(powerId);
            consumer.accept(skillBuilder);
            return skill(skillBuilder.build());
        }

        public GameItem build() {
            if (category == Category.AMMO && ammoOptions == null) {
                throw new IllegalStateException("Ammo items must define their ammo type");
            }
            if (projectileOptions != null && projectileOptions.consumesAmmo() && ammoOptions != null) {
                throw new IllegalStateException("An item cannot consume ammo and be ammo itself");
            }
            return new GameItem(this);
        }
    }

    public static final class ProjectileOptions {

        public enum Trigger {
            LEFT_CLICK,
            RIGHT_CLICK,
            BOTH,
            USE_RELEASE,
            USE_HELD
        }

        private final Trigger trigger;
        private final EntityType projectileType;
        private final double speed;
        private final double spread;
        private final boolean hasGravity;
        private final long lifetimeTicks;
        private final Long blockLifetimeTicks;
        private final long cooldownTicks;
        private final boolean allowOffHand;
        private final AmmoType ammoType;
        private final int ammoPerShot;
        private final long chargeTicks;

        private ProjectileOptions(Trigger trigger,
                                  EntityType projectileType,
                                  double speed,
                                  double spread,
                                  boolean hasGravity,
                                  long lifetimeTicks,
                                  Long blockLifetimeTicks,
                                  long cooldownTicks,
                                  boolean allowOffHand,
                                  AmmoType ammoType,
                                  int ammoPerShot,
                                  long chargeTicks) {
            this.trigger = trigger;
            this.projectileType = projectileType;
            this.speed = speed;
            this.spread = spread;
            this.hasGravity = hasGravity;
            this.lifetimeTicks = lifetimeTicks;
            this.blockLifetimeTicks = blockLifetimeTicks;
            this.cooldownTicks = cooldownTicks;
            this.allowOffHand = allowOffHand;
            this.ammoType = ammoType;
            this.ammoPerShot = ammoPerShot;
            this.chargeTicks = chargeTicks;
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

        public long cooldownTicks() {
            return cooldownTicks;
        }

        public boolean allowOffHand() {
            return allowOffHand;
        }

        public AmmoType ammoType() {
            return ammoType;
        }

        public int ammoPerShot() {
            return ammoPerShot;
        }

        public long chargeTicks() {
            return chargeTicks;
        }

        public boolean consumesAmmo() {
            return ammoType != null && ammoPerShot > 0;
        }

        public ProjectileLaunchConfig toLaunchConfig() {
            return toLaunchConfig(1.0D);
        }

        public ProjectileLaunchConfig toLaunchConfig(double chargeMultiplier) {
            double multiplier = Math.max(0.1D, chargeMultiplier);
            double scaledSpeed = speed * multiplier;
            double scaledSpread = Math.max(0.0D, spread / Math.max(1.0D, multiplier));

            ProjectileLaunchConfig.Builder builder = ProjectileLaunchConfig.builder(projectileType)
                    .speed(scaledSpeed)
                    .spread(scaledSpread)
                    .hasGravity(hasGravity)
                    .lifetimeTicks(lifetimeTicks);
            if (blockLifetimeTicks != null) {
                builder.blockLifetimeTicks(blockLifetimeTicks);
            }
            return builder.build();
        }

        public static final class Builder {
            private Trigger trigger = Trigger.RIGHT_CLICK;
            private EntityType projectileType;
            private double speed = 1.2D;
            private double spread = 0.0D;
            private boolean hasGravity = true;
            private long lifetimeTicks = ProjectileLaunchConfig.DEFAULT_LIFETIME_TICKS;
            private Long blockLifetimeTicks;
            private long cooldownTicks = 10L;
            private boolean allowOffHand = false;
            private AmmoType ammoType;
            private int ammoPerShot = 1;
            private long chargeTicks = 0L;

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

            public Builder cooldownTicks(long cooldownTicks) {
                this.cooldownTicks = cooldownTicks;
                return this;
            }

            public Builder allowOffHand(boolean allowOffHand) {
                this.allowOffHand = allowOffHand;
                return this;
            }

            public Builder ammoRequirement(AmmoType ammoType, int amount) {
                this.ammoType = Objects.requireNonNull(ammoType, "ammoType");
                this.ammoPerShot = Math.max(1, amount);
                return this;
            }

            public Builder chargeTicks(long chargeTicks) {
                this.chargeTicks = Math.max(0L, chargeTicks);
                return this;
            }

            public ProjectileOptions build() {
                if (projectileType == null) {
                    throw new IllegalStateException("Projectile type must be specified for launcher items");
                }
                double resolvedSpeed = speed > 0 ? speed : 1.0D;
                double resolvedSpread = Math.max(0.0D, spread);
                long resolvedCooldown = Math.max(0L, cooldownTicks);
                long resolvedLifetime = lifetimeTicks > 0 ? lifetimeTicks : ProjectileLaunchConfig.DEFAULT_LIFETIME_TICKS;
                int resolvedAmmo = ammoPerShot > 0 ? ammoPerShot : 1;
                long resolvedCharge = chargeTicks > 0 ? chargeTicks : 0;
                return new ProjectileOptions(
                        trigger,
                        projectileType,
                        resolvedSpeed,
                        resolvedSpread,
                        hasGravity,
                        resolvedLifetime,
                        blockLifetimeTicks,
                        resolvedCooldown,
                        allowOffHand,
                        ammoType,
                        resolvedAmmo,
                        resolvedCharge
                );
            }
        }
    }

    public static final class AmmoOptions {
        private final AmmoType type;

        private AmmoOptions(AmmoType type) {
            this.type = type;
        }

        public AmmoType type() {
            return type;
        }

        public static final class Builder {
            private AmmoType type;

            public Builder type(AmmoType type) {
                this.type = Objects.requireNonNull(type, "type");
                return this;
            }

            public AmmoOptions build() {
                if (type == null) {
                    throw new IllegalStateException("Ammo type must be provided");
                }
                return new AmmoOptions(type);
            }
        }
    }
}

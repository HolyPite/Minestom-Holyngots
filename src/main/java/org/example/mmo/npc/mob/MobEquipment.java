package org.example.mmo.npc.mob;

import net.minestom.server.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Describes the equipment loadout an archetype should wear.
 * Stores item ids referencing the item registry to avoid duplicating ItemStack instances.
 */
public final class MobEquipment {

    public static final MobEquipment EMPTY = new MobEquipment(Collections.emptyMap());

    private final Map<EquipmentSlot, String> equipment;

    public MobEquipment(Map<EquipmentSlot, String> equipment) {
        EnumMap<EquipmentSlot, String> copy = new EnumMap<>(EquipmentSlot.class);
        copy.putAll(equipment);
        this.equipment = Collections.unmodifiableMap(copy);
    }

    public Map<EquipmentSlot, String> equipment() {
        return equipment;
    }

    public Optional<String> itemFor(@NotNull EquipmentSlot slot) {
        return Optional.ofNullable(equipment.get(slot));
    }

    public boolean isEmpty() {
        return equipment.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EnumMap<EquipmentSlot, String> values = new EnumMap<>(EquipmentSlot.class);

        public Builder equip(EquipmentSlot slot, String itemId) {
            values.put(slot, itemId);
            return this;
        }

        public MobEquipment build() {
            return new MobEquipment(values);
        }
    }
}

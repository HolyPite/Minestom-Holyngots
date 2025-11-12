package org.example.mmo.item.skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for dynamic values consumed by a power (radius, damage, etc.).
 */
public final class PowerParameters {

    private final Map<String, Double> doubles;

    private PowerParameters(Map<String, Double> doubles) {
        this.doubles = doubles;
    }

    public double get(String key, double defaultValue) {
        return doubles.getOrDefault(key, defaultValue);
    }

    public Map<String, Double> asMap() {
        return doubles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PowerParameters of(Map<String, Double> values) {
        return new PowerParameters(Collections.unmodifiableMap(new HashMap<>(values)));
    }

    public static final class Builder {
        private final Map<String, Double> values = new HashMap<>();

        public Builder put(String key, double value) {
            values.put(key, value);
            return this;
        }

        public PowerParameters build() {
            return PowerParameters.of(values);
        }
    }
}

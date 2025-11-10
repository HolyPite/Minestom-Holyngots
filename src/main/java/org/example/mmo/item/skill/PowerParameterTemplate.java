package org.example.mmo.item.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Utility builder for level-based parameter scaling.
 */
public final class PowerParameterTemplate implements SkillParameterProvider {

    private final Map<String, LevelFunction> functions;

    private PowerParameterTemplate(Map<String, LevelFunction> functions) {
        this.functions = functions;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public PowerParameters parametersForLevel(int level) {
        final int resolvedLevel = Math.max(1, level);
        PowerParameters.Builder builder = PowerParameters.builder();
        functions.forEach((key, fn) -> builder.put(key, fn.value(resolvedLevel)));
        return builder.build();
    }

    public static final class Builder {
        private final Map<String, LevelFunction> functions = new HashMap<>();

        public Builder constant(String key, double value) {
            functions.put(key, lvl -> value);
            return this;
        }

        public Builder linear(String key, double baseValue, double perLevel) {
            functions.put(key, lvl -> baseValue + perLevel * (Math.max(1, lvl) - 1));
            return this;
        }

        public Builder clampedLinear(String key, double baseValue, double perLevel, double min, double max) {
            functions.put(key, lvl -> clamp(baseValue + perLevel * (Math.max(1, lvl) - 1), min, max));
            return this;
        }

        public Builder exponential(String key, double baseValue, double growthFactor) {
            functions.put(key, lvl -> baseValue * Math.pow(growthFactor, Math.max(0, lvl - 1)));
            return this;
        }

        public Builder step(String key, double... values) {
            functions.put(key, lvl -> {
                if (values.length == 0) return 0d;
                int index = Math.min(values.length - 1, Math.max(1, lvl) - 1);
                return values[index];
            });
            return this;
        }

        public Builder inverse(String key, double startValue, double dropPerLevel, double minValue) {
            functions.put(key, lvl -> Math.max(minValue, startValue - dropPerLevel * (Math.max(1, lvl) - 1)));
            return this;
        }

        public Builder sequence(String key, IntFunction<Double> generator) {
            functions.put(key, lvl -> generator.apply(Math.max(1, lvl)));
            return this;
        }

        public Builder custom(String key, LevelFunction function) {
            functions.put(key, function);
            return this;
        }

        public PowerParameterTemplate build() {
            return new PowerParameterTemplate(Map.copyOf(functions));
        }

        private static double clamp(double value, double min, double max) {
            if (Double.isNaN(value)) return min;
            return Math.max(min, Math.min(max, value));
        }
    }

    @FunctionalInterface
    public interface LevelFunction {
        double value(int level);
    }
}

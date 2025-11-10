package org.example.mmo.item.skill;

import java.util.HashMap;
import java.util.Map;

public final class PowerParameterTemplate implements SkillParameterProvider {

    private final Map<String, Formula> formulas;

    private PowerParameterTemplate(Map<String, Formula> formulas) {
        this.formulas = formulas;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public PowerParameters parametersForLevel(int level) {
        level = Math.max(1, level);
        PowerParameters.Builder builder = PowerParameters.builder();
        for (Map.Entry<String, Formula> entry : formulas.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().value(level));
        }
        return builder.build();
    }

    public static final class Builder {
        private final Map<String, Formula> formulas = new HashMap<>();

        public Builder constant(String key, double value) {
            formulas.put(key, new Formula(value, 0.0));
            return this;
        }

        public Builder linear(String key, double baseValue, double perLevel) {
            formulas.put(key, new Formula(baseValue, perLevel));
            return this;
        }

        public PowerParameterTemplate build() {
            return new PowerParameterTemplate(Map.copyOf(formulas));
        }
    }

    private record Formula(double base, double perLevel) {
        double value(int level) {
            return base + perLevel * (Math.max(1, level) - 1);
        }
    }
}

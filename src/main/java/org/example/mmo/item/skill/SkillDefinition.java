package org.example.mmo.item.skill;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

public record SkillDefinition(String powerId,
                              Set<SkillTrigger> triggers,
                              int level,
                              Duration cooldown,
                              PowerParameters parameters) {

    public SkillDefinition {
        if (powerId == null || powerId.isBlank()) {
            throw new IllegalArgumentException("powerId must not be blank");
        }
        if (triggers == null || triggers.isEmpty()) {
            throw new IllegalArgumentException("SkillDefinition requires at least one trigger");
        }
        level = Math.max(1, level);
        cooldown = cooldown == null ? Duration.ZERO : cooldown;
        parameters = parameters == null ? PowerParameters.builder().build() : parameters;
        triggers = EnumSet.copyOf(triggers);
    }

    public static Builder builder(String powerId) {
        return new Builder(powerId);
    }

    public static final class Builder {
        private final String powerId;
        private final EnumSet<SkillTrigger> triggers = EnumSet.noneOf(SkillTrigger.class);
        private int level = 1;
        private Duration cooldown = Duration.ZERO;
        private PowerParameters parameters = PowerParameters.builder().build();

        private Builder(String powerId) {
            this.powerId = powerId;
        }

        public Builder addTrigger(SkillTrigger trigger) {
            this.triggers.add(trigger);
            return this;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder cooldown(Duration cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder parameters(PowerParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public SkillDefinition build() {
            return new SkillDefinition(powerId, triggers, level, cooldown, parameters);
        }
    }
}

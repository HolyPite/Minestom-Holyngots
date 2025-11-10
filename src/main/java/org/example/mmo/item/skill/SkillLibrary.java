package org.example.mmo.item.skill;

import org.example.mmo.item.skill.SkillDefinition;
import org.example.mmo.item.skill.SkillInstance;
import org.example.mmo.item.skill.PowerRegistry;

import java.util.Optional;

public final class SkillLibrary {

    private SkillLibrary() {}

    public static Optional<SkillInstance> resolve(SkillDefinition definition) {
        var power = PowerRegistry.resolve(definition.powerId());
        if (power == null) {
            return Optional.empty();
        }
        return Optional.of(new SkillInstance(definition, power));
    }
}

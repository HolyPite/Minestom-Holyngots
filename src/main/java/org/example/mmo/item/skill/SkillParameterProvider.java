package org.example.mmo.item.skill;

@FunctionalInterface
public interface SkillParameterProvider {

    PowerParameters parametersForLevel(int level);

    static SkillParameterProvider constant(PowerParameters parameters) {
        return lvl -> parameters;
    }
}

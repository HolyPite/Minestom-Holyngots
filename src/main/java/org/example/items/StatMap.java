package org.example.items;

import net.minestom.server.entity.attribute.Attribute;

import java.util.EnumMap;
import java.util.Map;

public final class StatMap extends EnumMap<StatType, Integer> {

    public StatMap() { super(StatType.class); }

    /** Ajoute ou remplace la valeur d’une stat */
    public StatMap with(StatType t, int value){
        put(t, value);
        return this;
    }

    /** Incrémente (ou crée) */
    public void add(StatType t, int delta){
        put(t, getOrDefault(t, 0) + delta);
    }
    // CombatEngine.java  (en haut du fichier)
    public static final Map<StatType, Attribute> ATTR_MAP = Map.of(
            StatType.ATTACK, Attribute.ATTACK_DAMAGE,
            StatType.ARMOR , Attribute.ARMOR,
            StatType.HEALTH, Attribute.MAX_HEALTH,
            StatType.KNOCKBACK_RES, Attribute.KNOCKBACK_RESISTANCE,
            StatType.KNOCKBACK, Attribute.ATTACK_KNOCKBACK
    );

}

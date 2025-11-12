package org.example.mmo.item.datas;

import static org.example.mmo.item.datas.StatType.ValueKind.FLAT;
import static org.example.mmo.item.datas.StatType.ValueKind.PERCENT;
import static org.example.mmo.item.datas.StatType.ValueKind.PROBA;

public enum StatType {

    /* -------- FLAT -------- */
    ATTACK("Attaque", FLAT),
    ARMOR("Armure",  FLAT),
    HEALTH("Santé",  FLAT),
    HP_REGEN("Régén. PV", FLAT),

    /* -------- PROBA ------- */
    CRIT_CHANCE("Chance crit.",  PROBA),
    KNOCKBACK_RES("Anti-recul",  PROBA),
    DODGE("Esquive",            PROBA),
    STUN_CHANCE("Chance étourdir", PROBA),
    ARMOR_PEN("Perforation",    PROBA),
    LIFESTEAL("Vol de vie",     PROBA),

    /* -------- PERCENT ----- */
    KNOCKBACK("Recul",  PERCENT),
    CRIT_VALUE("Dégâts crit.",  PERCENT),
    ATTACK_SPEED("Vit. attaque",PERCENT);


    /* champs --------------------------------------------------- */
    public final String    label;
    public final ValueKind kind;

    StatType(String label, ValueKind kind) {
        this.label = label;
        this.kind  = kind;
    }

    /** Catégorie logico-mathématique de la statistique */
    public enum ValueKind {
        FLAT,          // somme directe
        PROBA,       // 0–100 %
        PERCENT     // ≥ 0, souvent > 100 %
    }

}

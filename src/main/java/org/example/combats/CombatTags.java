package org.example.combats;

import net.minestom.server.tag.Tag;

public interface CombatTags {
    Tag<Long> LAST_ATTACK_TICK = Tag.Long("last_attack_tick");
    Tag<Long> LAST_VICTIM_TICK = Tag.Long("last_victim_tick");
}

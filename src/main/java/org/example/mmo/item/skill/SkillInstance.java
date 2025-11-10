package org.example.mmo.item.skill;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillInstance {

    private final SkillDefinition definition;
    private final Power power;
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

    public SkillInstance(@NotNull SkillDefinition definition, @NotNull Power power) {
        this.definition = definition;
        this.power = power;
    }

    public SkillDefinition definition() {
        return definition;
    }

    public boolean supports(SkillTrigger trigger) {
        return definition.triggers().contains(trigger);
    }

    public boolean tryActivate(Player player,
                               ItemStack stack,
                               SkillTrigger trigger,
                               SkillTriggerData data) {
        if (!supports(trigger)) {
            return false;
        }
        long now = System.currentTimeMillis();
        long nextAllowed = lastUse.getOrDefault(player.getUuid(), 0L);
        if (now < nextAllowed) {
            return false;
        }
        PowerContext context = new PowerContext(player, stack, trigger, data, definition.level());
        power.execute(context, definition.parameters());
        long cooldownMillis = definition.cooldown().toMillis();
        if (cooldownMillis > 0) {
            lastUse.put(player.getUuid(), now + cooldownMillis);
        }
        return true;
    }
}

package org.example.mmo.item.skill;

import net.minestom.server.entity.LivingEntity;
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

    public SkillActivationResult tryActivate(LivingEntity entity,
                                             ItemStack stack,
                                             SkillTrigger trigger,
                                             SkillTriggerData data) {
        if (!supports(trigger)) {
            return SkillActivationResult.UNSUPPORTED;
        }
        long now = System.currentTimeMillis();
        long nextAllowed = lastUse.getOrDefault(entity.getUuid(), 0L);
        if (now < nextAllowed) {
            return SkillActivationResult.onCooldown(definition.powerId(), nextAllowed - now);
        }
        PowerContext context = new PowerContext(entity, stack, trigger, data, definition.level());
        power.execute(context, definition.resolveParameters());
        long cooldownMillis = definition.cooldown().toMillis();
        if (cooldownMillis > 0) {
            lastUse.put(entity.getUuid(), now + cooldownMillis);
        }
        return SkillActivationResult.success(definition.powerId());
    }
}

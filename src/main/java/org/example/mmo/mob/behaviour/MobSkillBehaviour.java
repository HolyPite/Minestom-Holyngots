package org.example.mmo.mob.behaviour;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.skill.SkillInstance;
import org.example.mmo.item.skill.SkillTrigger;
import org.example.mmo.item.skill.SkillTriggerData;
import org.example.mmo.mob.MobBehaviourAdapter;
import org.example.mmo.mob.MobInstance;

import java.util.EnumSet;
import java.util.EnumSet;
import java.util.Set;

public final class MobSkillBehaviour extends MobBehaviourAdapter {

    private static final ItemStack EMPTY_STACK = ItemStack.AIR;

    private final SkillInstance skill;
    private final EnumSet<SkillTrigger> triggers;

    public MobSkillBehaviour(SkillInstance skill, Set<SkillTrigger> triggers) {
        this.skill = skill;
        this.triggers = triggers.isEmpty() ? EnumSet.noneOf(SkillTrigger.class) : EnumSet.copyOf(triggers);
    }

    private boolean accepts(SkillTrigger trigger) {
        return triggers.contains(trigger) && skill.supports(trigger);
    }

    private void activate(MobInstance instance, SkillTrigger trigger, SkillTriggerData data) {
        if (!accepts(trigger)) {
            return;
        }
        instance.resolveEntity().ifPresent(entity -> skill.tryActivate(entity, EMPTY_STACK, trigger, data));
    }

    @Override
    public void onSpawn(MobInstance instance) {
        activate(instance, SkillTrigger.ENTITY_SPAWN, new SkillTriggerData.SpawnData());
    }

    @Override
    public void onTick(MobInstance instance, long tickTime) {
        activate(instance, SkillTrigger.ENTITY_TICK, new SkillTriggerData.TickData(tickTime));
    }

    @Override
    public void onAggro(MobInstance instance, Entity target) {
        activate(instance, SkillTrigger.ENTITY_AGGRO, new SkillTriggerData.EntityTargetData(target));
    }

    @Override
    public void onDamaged(MobInstance instance, Damage damage) {
        activate(instance, SkillTrigger.ENTITY_DAMAGED, new SkillTriggerData.DamageData(damage));
    }

    @Override
    public void onDeath(MobInstance instance, Entity killer) {
        activate(instance, SkillTrigger.ENTITY_DEATH, new SkillTriggerData.DeathData(killer));
    }
}

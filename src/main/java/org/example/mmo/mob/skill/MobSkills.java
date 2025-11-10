package org.example.mmo.mob.skill;

import org.example.mmo.item.power.powers.BulwarkShieldPower;
import org.example.mmo.item.power.powers.DashPower;
import org.example.mmo.item.power.powers.FrenzyPower;
import org.example.mmo.item.power.powers.IgniteBurstPower;
import org.example.mmo.item.power.powers.TeleportPower;
import org.example.mmo.item.skill.PowerParameters;
import org.example.mmo.item.skill.SkillDefinition;
import org.example.mmo.item.skill.SkillTrigger;

import java.time.Duration;
import java.util.Arrays;

public final class MobSkills {

    private MobSkills() {}

    public static SkillDefinition dash(double speed, double verticalScale, Duration cooldown, SkillTrigger... triggers) {
        SkillDefinition.Builder builder = SkillDefinition.builder(DashPower.ID)
                .level(1)
                .cooldown(cooldown)
                .parameters(PowerParameters.builder()
                        .put("speed", speed)
                        .put("vertical_scale", verticalScale)
                        .build());
        Arrays.stream(triggers).forEach(builder::addTrigger);
        return builder.build();
    }

    public static SkillDefinition teleport(double distance, double verticalOffset, Duration cooldown, SkillTrigger... triggers) {
        SkillDefinition.Builder builder = SkillDefinition.builder(TeleportPower.ID)
                .level(1)
                .cooldown(cooldown)
                .parameters(PowerParameters.builder()
                        .put("distance", distance)
                        .put("vertical_offset", verticalOffset)
                        .build());
        Arrays.stream(triggers).forEach(builder::addTrigger);
        return builder.build();
    }

    public static SkillDefinition ignite(double radius, double yOffset, Duration cooldown, SkillTrigger... triggers) {
        SkillDefinition.Builder builder = SkillDefinition.builder(IgniteBurstPower.ID)
                .level(1)
                .cooldown(cooldown)
                .parameters(PowerParameters.builder()
                        .put("radius", radius)
                        .put("y_offset", yOffset)
                        .build());
        Arrays.stream(triggers).forEach(builder::addTrigger);
        return builder.build();
    }

    public static SkillDefinition frenzy(double thresholdRatio,
                                         double durationTicks,
                                         double strengthAmplifier,
                                         double speedAmplifier,
                                         Duration cooldown,
                                         SkillTrigger... triggers) {
        SkillDefinition.Builder builder = SkillDefinition.builder(FrenzyPower.ID)
                .level(1)
                .cooldown(cooldown)
                .parameters(PowerParameters.builder()
                        .put("threshold_ratio", thresholdRatio)
                        .put("duration_ticks", durationTicks)
                        .put("strength_amplifier", strengthAmplifier)
                        .put("speed_amplifier", speedAmplifier)
                        .build());
        Arrays.stream(triggers).forEach(builder::addTrigger);
        return builder.build();
    }

    public static SkillDefinition bulwark(double amplifier, double durationTicks, Duration cooldown, SkillTrigger... triggers) {
        SkillDefinition.Builder builder = SkillDefinition.builder(BulwarkShieldPower.ID)
                .level(1)
                .cooldown(cooldown)
                .parameters(PowerParameters.builder()
                        .put("amplifier", amplifier)
                        .put("duration_ticks", durationTicks)
                        .build());
        Arrays.stream(triggers).forEach(builder::addTrigger);
        return builder.build();
    }
}

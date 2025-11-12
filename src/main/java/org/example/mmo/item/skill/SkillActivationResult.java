package org.example.mmo.item.skill;

public record SkillActivationResult(SkillActivationStatus status,
                                    long cooldownRemainingMs,
                                    String powerId) {

    public static final SkillActivationResult UNSUPPORTED =
            new SkillActivationResult(SkillActivationStatus.UNSUPPORTED, 0L, null);

    public static SkillActivationResult success(String powerId) {
        return new SkillActivationResult(SkillActivationStatus.SUCCESS, 0L, powerId);
    }

    public static SkillActivationResult onCooldown(String powerId, long remainingMs) {
        return new SkillActivationResult(SkillActivationStatus.ON_COOLDOWN, Math.max(0L, remainingMs), powerId);
    }

    public boolean isSuccess() {
        return status == SkillActivationStatus.SUCCESS;
    }

    public boolean isOnCooldown() {
        return status == SkillActivationStatus.ON_COOLDOWN;
    }
}

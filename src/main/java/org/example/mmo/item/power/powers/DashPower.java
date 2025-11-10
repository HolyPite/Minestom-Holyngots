package org.example.mmo.item.power.powers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;

@PowerId(DashPower.ID)
public final class DashPower implements Power {

    public static final String ID = "core:dash";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        LivingEntity entity = context.entity();
        double speed = Math.max(0.1, parameters.get("speed", 8.0));
        double verticalScale = parameters.get("vertical_scale", 1.0);

        Vec dir = forward(entity.getPosition());
        Vec velocity = new Vec(dir.x() * speed, dir.y() * speed * verticalScale, dir.z() * speed);
        entity.setVelocity(velocity);
    }

    private static Vec forward(Pos pos) {
        double yaw = Math.toRadians(pos.yaw());
        double pitch = Math.toRadians(pos.pitch());
        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);
        Vec vec = new Vec(x, y, z);
        double length = Math.sqrt(vec.x() * vec.x() + vec.y() * vec.y() + vec.z() * vec.z());
        if (length == 0) {
            return new Vec(0, 0, 0);
        }
        return vec.div(length);
    }
}

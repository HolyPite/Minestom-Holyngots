package org.example.mmo.item.power.powers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;

@PowerId(TeleportPower.ID)
public final class TeleportPower implements Power {

    public static final String ID = "core:teleport";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        LivingEntity entity = context.entity();
        double distance = Math.max(1.0, parameters.get("distance", 8.0));
        double verticalOffset = parameters.get("vertical_offset", 0.0);

        Pos current = entity.getPosition();
        Vec direction = forward(current);
        Pos target = current.add(direction.mul(distance))
                .withY(current.y() + direction.y() * distance + verticalOffset);

        entity.teleport(target);
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

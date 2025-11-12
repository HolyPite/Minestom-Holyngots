package org.example.mmo.item.power.powers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.example.mmo.item.power.PowerId;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerContext;
import org.example.mmo.item.skill.PowerParameters;

@PowerId(IgniteBurstPower.ID)
public final class IgniteBurstPower implements Power {

    public static final String ID = "core:ignite_burst";

    @Override
    public void execute(PowerContext context, PowerParameters parameters) {
        LivingEntity entity = context.entity();
        Instance instance = entity.getInstance();
        if (instance == null) {
            return;
        }
        double radius = Math.max(1.0, parameters.get("radius", 3.0));
        int yOffset = (int) Math.round(parameters.get("y_offset", 0.0));

        Pos pos = entity.getPosition();
        int centerX = (int) Math.floor(pos.x());
        int centerY = (int) Math.floor(pos.y()) + yOffset;
        int centerZ = (int) Math.floor(pos.z());

        int range = (int) Math.ceil(radius);
        double radiusSq = radius * radius;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    double distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > radiusSq) {
                        continue;
                    }
                    int x = centerX + dx;
                    int y = centerY + dy;
                    int z = centerZ + dz;
                    Block current = instance.getBlock(x, y, z);
                    if (!current.isAir()) {
                        continue;
                    }
                    Block below = instance.getBlock(x, y - 1, z);
                    if (below.isAir()) {
                        continue;
                    }
                    instance.setBlock(x, y, z, Block.FIRE);
                }
            }
        }
    }
}

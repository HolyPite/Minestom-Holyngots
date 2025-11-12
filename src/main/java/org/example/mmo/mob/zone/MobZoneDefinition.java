package org.example.mmo.mob.zone;

import net.minestom.server.coordinate.Pos;

import java.time.Duration;
import java.util.List;

public record MobZoneDefinition(String id,
                                String displayName,
                                Pos center,
                                double radius,
                                List<String> mobIds,
                                List<Integer> maxAlive,
                                Duration respawnDelay) {
}

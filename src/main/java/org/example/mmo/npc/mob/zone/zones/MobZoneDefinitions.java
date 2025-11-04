package org.example.mmo.npc.mob.zone.zones;

import net.minestom.server.coordinate.Pos;
import org.example.mmo.npc.mob.zone.MobZoneDefinition;

import java.time.Duration;
import java.util.List;

public final class MobZoneDefinitions {

    public static final List<MobZoneDefinition> ZONES = List.of(
            new MobZoneDefinition(
                    "wolf_grove",
                    "Clairiere des loups",
                    new Pos(52.5, 40, -28.5),
                    14.0,
                    List.of("forest_wolf"),
                    List.of(10),
                    Duration.ofSeconds(25)
            ),
            new MobZoneDefinition(
                    "bandit_camp",
                    "Campement des bandits",
                    new Pos(-36.0, 40, 18.0),
                    9.0,
                    List.of("bandit_skirmisher", "bandit_archer"),
                    List.of(6, 3),
                    Duration.ofSeconds(35)
            ),
            new MobZoneDefinition(
                    "sunken_sanctum",
                    "Sanctuaire englouti",
                    new Pos(18.0, 40.0, -72.0),
                    11.0,
                    List.of("sunken_guardian"),
                    List.of(4),
                    Duration.ofSeconds(30)
            )
    );

    private MobZoneDefinitions() {
    }
}

package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.time.Duration;
import java.util.List;

public final class SpeedTrial {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("Acceptez l'épreuve");
        step1.description = Component.text("L'archimage veut tester votre vitesse.");
        step1.startNpc = "wizard";
        step1.endNpc = "wizard";

        QuestStep step2 = new QuestStep();
        step2.name = Component.text("Atteignez les cercles de pouvoir");
        step2.description = Component.text("Vous avez 1 minute pour atteindre le premier cercle de pouvoir, puis le second.");
        step2.objectives = List.of(
                new LocationObjective(new Pos(20, 40, 20), 3, Component.text("Atteignez le cercle au sommet de la colline")),
                new LocationObjective(new Pos(-20, 40, -20), 3, Component.text("Atteignez le cercle dans la vallée"))
        );
        step2.duration = Duration.ofMinutes(1);
        step2.attemptLimit = 1;
        step2.failureDialogues = List.of(Component.text("Trop lent ! L'énergie s'est dissipée."));
        step2.endNpc = "wizard";

        QUEST = new Quest(
                "speed_trial",
                Component.text("L'Épreuve de Rapidité"),
                Component.text("Prouvez votre vitesse à l'archimage."),
                List.of(step1, step2)
        );

        QuestRegistry.register(QUEST);
    }
}

package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.time.Duration;
import java.util.List;

public final class RitualQuest {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("Le Rituel Arcanique");
        step1.description = Component.text("Maintenant que vous avez prouvé votre vitesse, l'archimage vous confie une tâche importante.");
        step1.startNpc = "wizard";
        step1.endNpc = "wizard";
        step1.prerequisites = List.of("speed_trial:2");
        step1.delay = Duration.ofMinutes(1);
        step1.delayDialogues = List.of(Component.text("Je dois préparer le rituel. Revenez dans un instant."));

        QuestStep step2 = new QuestStep();
        step2.name = Component.text("Trouvez le lieu du rituel");
        step2.description = Component.text("L'archimage vous a indiqué un lieu de pouvoir. Trouvez-le.");
        step2.objectives = List.of(
                new LocationObjective(new Pos(-30, 40, -30), 4, step2.description)
        );
        step2.endNpc = "wizard";

        QUEST = new Quest(
                "ritual_quest",
                Component.text("Le Rituel"),
                Component.text("Aidez l'archimage à accomplir un rituel important."),
                List.of(step1, step2)
        );

        QuestRegistry.register(QUEST);
    }
}

package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

public final class LearningQuest {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("Une nouvelle leçon");
        step1.description = Component.text("L'archimage a quelque chose à vous enseigner.");
        step1.startNpc = "wizard";
        step1.endNpc = "wizard";
        step1.objectives = List.of(
                new TalkObjective("wizard", Component.text("Écouter l'Archimage"), List.of(Component.text("La magie est une danse subtile...")))
        );

        QuestStep step2 = new QuestStep();
        step2.name = Component.text("Écoutez attentivement");
        step2.description = Component.text("L'archimage vous explique les bases de la magie. Retournez le voir quand vous serez prêt.");
        step2.startNpc = "wizard";
        step2.endNpc = "wizard";
        step2.objectives = List.of(
                new TalkObjective("wizard", Component.text("Confirmer votre compréhension"), List.of(Component.text("Avez-vous bien tout saisi ?")))
        );

        QUEST = new Quest(
                "learning_quest",
                Component.text("L'Apprentissage"),
                Component.text("Écoutez les enseignements de l'archimage."),
                List.of(step1, step2)
        );

        QuestRegistry.register(QUEST);
    }

    private LearningQuest() {}
}

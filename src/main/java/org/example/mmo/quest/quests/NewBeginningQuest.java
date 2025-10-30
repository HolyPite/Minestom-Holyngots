package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

public final class NewBeginningQuest {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("Faites vos premiers pas");
        step1.description = Component.text("Le monde s'ouvre à vous. Allez parler au Guide Ancien qui se trouve près de vous pour commencer votre aventure.");
        step1.objectives = List.of(
                new TalkObjective("guide", Component.text("Parler au Guide Ancien"), List.of(Component.text("Bienvenue, aventurier.")))
        );
        step1.endNpc = "guide";

        QUEST = new Quest(
                "new_beginning",
                Component.text("Un Nouveau Départ"),
                Component.text("Une quête d'introduction à votre aventure."),
                List.of(step1)
        );

        QuestRegistry.register(QUEST);
    }
}

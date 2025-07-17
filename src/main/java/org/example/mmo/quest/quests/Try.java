package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.item.items.WEAPON.Excalibur;
import org.example.mmo.quest.*;

import java.util.List;

/**
 * Simple demonstration quest containing three steps.
 */
public final class Try {
    public static final Quest QUEST;

    static {
        QuestStep s1 = new QuestStep();
        s1.description = Component.text("Parlez au guide.");
        s1.startNpc = "guide";
        s1.endNpc = "guide";

        QuestStep s2 = new QuestStep();
        s2.description = Component.text("Trouvez l'épée Excalibur.");
        s2.startNpc = "guide";
        s2.endNpc = "guide";
        s2.objectives = List.of(Excalibur.excalibur);

        QuestStep s3 = new QuestStep();
        s3.description = Component.text("Retournez voir le guide.");
        s3.startNpc = "guide";
        s3.endNpc = "guide";
        s3.rewards = List.of(Excalibur.excalibur);

        QUEST = new Quest(
                "try",
                Component.text("Première quête"),
                Component.text("Quête d'exemple"),
                List.of(s1, s2, s3)
        );

        QuestRegistry.register(QUEST);
    }

    private Try() {}
}

package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.time.Duration;
import java.util.List;

/**
 * Simple recovery quest automatically triggered when the player échoue l'Ǹpreuve de RǸsilience.
 * Helps validating the failure redirection flow.
 */
public final class ResilienceRecoveryQuest {
    public static final Quest QUEST;

    static {
        QuestStep debrief = new QuestStep();
        debrief.name = Component.text("Analyse de l'Ǹchec");
        debrief.description = Component.text("Retournez voir le Guide pour analyser l'Ǹchec et vous recentrer.");
        debrief.startNpc = null; // Auto-start when redirected
        debrief.endNpc = "guide";
        debrief.objectives = List.of(
                new TalkObjective(
                        "guide",
                        Component.text("Parler au Guide pour se recentrer"),
                        List.of(
                                Component.text("Chaque chute est une leçon. Respire et reprends la route.", NamedTextColor.DARK_PURPLE)
                        )
                )
        );
        debrief.successDialogues = List.of(
                Component.text("Tu es prǸt �� retourner �� l'entrainement. Que la prochaine tentative soit la bonne !", NamedTextColor.GOLD)
        );

        QUEST = new Quest(
                "resilience_recovery",
                Component.text("Rebondir après l'Ǹchec"),
                Component.text("Une discussion rapide pour transformer l'Ǹchec en apprentissage."),
                List.of(debrief),
                true,
                Duration.ofSeconds(10)
        );

        QuestRegistry.register(QUEST);
    }

    private ResilienceRecoveryQuest() {}
}

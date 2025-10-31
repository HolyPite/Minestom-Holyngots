package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.time.Duration;
import java.util.List;

/**
 * Quest used as an integration scenario for advanced quest-step features:
 * prerequisites, delay, timed objectives, attempt limits and failure redirection.
 */
public final class ResilienceTrialQuest {
    public static final Quest QUEST;

    static {
        QuestStep audience = new QuestStep();
        audience.name = Component.text("Audience du Guide");
        audience.description = Component.text("Revenez voir le Guide pour prouver que vous Ǹtes prǸt �� affronter la prochaine Ǹpreuve.");
        audience.startNpc = "guide";
        audience.endNpc = "guide";
        audience.prerequisites = List.of("new_beginning");
        audience.objectives = List.of(
                new TalkObjective(
                        "guide",
                        Component.text("Parler au Guide Ancien"),
                        List.of(
                                Component.text("Je ne l��se entrer que les Ǹmes pr��tes �� chanceler.", NamedTextColor.DARK_PURPLE)
                        )
                )
        );
        audience.successDialogues = List.of(
                Component.text("Ta dǸtermination me rassure. Prends maintenant le temps de te centrer.", NamedTextColor.GOLD)
        );

        QuestStep meditation = new QuestStep();
        meditation.name = Component.text("Moment de RǸflexion");
        meditation.description = Component.text("Rejoignez le promontoire pour vous recentrer avant de revenir voir le chasseur.");
        meditation.startNpc = "guide";
        meditation.endNpc = "hunter";
        meditation.delay = Duration.ofSeconds(5);
        meditation.delayDialogues = List.of(
                Component.text("Ne sois pas pressǸ. Inspire, expire... Reviens me voir quand ton esprit sera apaisǸ.", NamedTextColor.GRAY)
        );
        meditation.objectives = List.of(
                new LocationObjective(
                        new Pos(6.5, 42, -4.5),
                        3.0,
                        Component.text("Atteindre le promontoire d'observation")
                )
        );
        meditation.successDialogues = List.of(
                Component.text("Je sens en toi une force tranquille. File voir le chasseur, il t'attend.", NamedTextColor.YELLOW)
        );

        QuestStep hunt = new QuestStep();
        hunt.name = Component.text("Chasse ChronomǸtrǸe");
        hunt.description = Component.text("Le chasseur veut voir si tu sais rester lucide sous pression : Ǹlimine 2 squelettes avant la fin du compte �� rebours.");
        hunt.startNpc = "hunter";
        hunt.endNpc = "hunter";
        hunt.duration = Duration.ofSeconds(45);
        hunt.attemptLimit = 2;
        hunt.failureRedirection = true;
        hunt.failureRedirectionQuest = "resilience_recovery";
        hunt.objectives = List.of(
                new KillObjective(
                        EntityType.SKELETON,
                        2,
                        "resilience_trial_skeletons",
                        Component.text("Abattre 2 squelettes")
                )
        );
        hunt.waitingDialogues = List.of(
                Component.text("Pas encore. Tu dois encore te dǸbarrasser de ces squelettes.", NamedTextColor.GRAY)
        );
        hunt.failureDialogues = List.of(
                Component.text("Le temps t'a filǸ entre les doigts. Analyse ton erreur avant de revenir.", NamedTextColor.RED)
        );
        hunt.successDialogues = List.of(
                Component.text("Belle maitrise ! Reste sur cette voie et tu deviendras un chasseur accompli.", NamedTextColor.GREEN)
        );

        QUEST = new Quest(
                "resilience_trial",
                Component.text("Ǹpreuve de RǸsilience"),
                Component.text("Une suite d��tapes pour tester patience, discipline et contr��le du stress."),
                List.of(audience, meditation, hunt),
                2,
                false,
                Duration.ZERO
        );

        QuestRegistry.register(QUEST);
    }

    private ResilienceTrialQuest() {}
}

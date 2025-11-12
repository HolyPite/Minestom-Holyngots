package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.example.mmo.item.ItemRegistry;
import org.example.mmo.item.items.MATERIALS.Bone;
import org.example.mmo.quest.objectives.FetchObjective;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.objectives.LocationObjective;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.rewards.ItemReward;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

public final class TheGrandTour {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("La Grande Tournée");
        step1.description = Component.text("Le chasseur veut tester vos compétences polyvalentes.");
        step1.startNpc = "hunter";
        step1.endNpc = "hunter";

        step1.objectives = List.of(
                new TalkObjective("wizard", Component.text("Consultez l'Archimage"), List.of(Component.text("Le chasseur vous envoie ? Intéressant..."))),
                new KillObjective("bandit_skirmisher", 5, "grand_tour_bandits", Component.text("Éliminez 5 bandits maraudeurs")),
                new LocationObjective(new Pos(-15, 40, 15), 3, Component.text("Trouvez le point de vue caché"))
        );

        step1.waitingDialogues = List.of(Component.text("Vous n'avez pas encore terminé toutes vos tâches."));
        step1.successDialogues = List.of(Component.text("Impressionnant. Vous êtes un aventurier accompli."));
        step1.rewards = List.of(new ItemReward(List.of(ItemRegistry.byId("admin_slayer"))));

        QUEST = new Quest(
                "grand_tour",
                Component.text("La Grande Tournée"),
                Component.text("Prouvez votre polyvalence au chasseur."),
                List.of(step1)
        );

        QuestRegistry.register(QUEST);
    }
}

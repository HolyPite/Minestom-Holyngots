package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.quest.objectives.KillObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

/**
 * A quest that requires killing multiple types of monsters in a single step.
 */
public final class MonsterPlague {
    public static final Quest QUEST;

    static {
        QuestStep step1 = new QuestStep();
        step1.name = Component.text("Le Fléau des Monstres");
        step1.description = Component.text("Le vieux chasseur vous a demandé de réduire la menace des créatures hostiles dans les environs.");
        step1.startNpc = "hunter";
        step1.endNpc = "hunter";

        // This step has three objectives that must all be completed.
        step1.objectives = List.of(
                new KillObjective("forest_wolf", 4, "monster_plague_wolves", Component.text("Vaincre 4 loups des bois")),
                new KillObjective("bandit_skirmisher", 5, "monster_plague_skirmishers", Component.text("Terrasser 5 bandits maraudeurs")),
                new KillObjective("bandit_archer", 3, "monster_plague_archers", Component.text("Abattre 3 archers bandits"))
        );

        step1.waitingDialogues = List.of(Component.text("Le travail n'est pas encore terminé. Continuez la chasse !"));
        step1.successDialogues = List.of(Component.text("Excellent travail. La région est un peu plus sûre grâce à vous."));

        // For now, no reward is given, but you could add one here:
        // step1.rewards = List.of(new ItemReward(List.of(...)));

        QUEST = new Quest(
                "monster_plague",
                Component.text("Le Fléau des Monstres"),
                Component.text("Aidez le chasseur à sécuriser la zone."),
                List.of(step1)
        );

        QuestRegistry.register(QUEST);
    }
}

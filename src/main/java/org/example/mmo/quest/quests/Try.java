package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import org.example.mmo.item.items.WEAPON.Excalibur;
import org.example.mmo.quest.objectives.FetchObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.rewards.ItemReward;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

/**
 * Simple demonstration quest using the new modular system.
 */
public final class Try {
    public static final Quest QUEST;

    static {
        // Step 1: Talk to the guide
        QuestStep s1 = new QuestStep();
        s1.name = Component.text("La rencontre");
        s1.description = Component.text("Parlez au guide qui est apparu près de vous.");
        s1.startNpc = "guide";
        s1.endNpc = "guide";
        s1.successDialogues = List.of(Component.text("Ah, un nouvel aventurier ! J'ai quelque chose pour vous..."));

        // Step 2: Find the sword
        QuestStep s2 = new QuestStep();
        s2.name = Component.text("L'artefact perdu");
        s2.description = Component.text("Le guide vous a parlé d'une épée légendaire. Trouvez Excalibur.");
        s2.startNpc = "guide"; // You get this step from the guide
        s2.endNpc = "guide";   // You return to the guide
        s2.objectives = List.of(
                new FetchObjective(Excalibur.INSTANCE, 1, s2.description)
        );
        s2.waitingDialogues = List.of(Component.text("Vous n'avez pas encore trouvé l'épée, on dirait. Cherchez mieux !"));
        s2.successDialogues = List.of(Component.text("Incroyable ! C'est bien elle ! Vous êtes digne de la recevoir."));

        // Step 3: The reward (no real objective, just talking)
        QuestStep s3 = new QuestStep();
        s3.name = Component.text("La récompense");
        s3.description = Component.text("L'épée est à vous. Le guide vous la remet officiellement.");
        s3.startNpc = "guide";
        s3.endNpc = "guide";
        s3.rewards = List.of(
                new ItemReward(List.of(Excalibur.INSTANCE))
        );

        QUEST = new Quest(
                "try",
                Component.text("Première quête"),
                Component.text("Une quête d'exemple pour apprendre les bases."),
                List.of(s1, s2, s3)
        );

        QuestRegistry.register(QUEST);
    }
}

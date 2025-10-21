package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.DamageType;
import org.example.mmo.quest.Quest;
import org.example.mmo.quest.QuestRegistry;
import org.example.mmo.quest.QuestStep;
import org.example.mmo.quest.objectives.SlayObjective;

import java.util.List;

/**
 * An example quest demonstrating the SlayObjective.
 * This quest requires killing zombies with fire damage.
 */
public final class FirePurge {
    public static final Quest QUEST;

    static {
        QuestStep s1 = new QuestStep();
        s1.name = Component.text("Purification par le Feu");
        s1.description = Component.text("Un prêtre vous demande de purifier la crypte en éliminant 3 zombies par le feu.");
        s1.startNpc = "priest"; // Assume an NPC with this ID exists
        s1.endNpc = "priest";

        s1.objectives = List.of(
            new SlayObjective(
                EntityType.ZOMBIE,
                3,
                "fire_purge_zombies", // Unique progress counter ID
                s1.description,
                // The condition: the final blow's damage type must be ON_FIRE
                damage -> damage.getType() == DamageType.ON_FIRE
            )
        );

        s1.waitingDialogues = List.of(Component.text("Les morts-vivants doivent être purifiés par le feu ! Revenez quand ce sera fait."));
        s1.successDialogues = List.of(Component.text("Le feu a purifié leurs âmes. Merci, aventurier."));

        QUEST = new Quest(
            "fire_purge",
            Component.text("Purification par le Feu"),
            Component.text("Aidez le prêtre à nettoyer la crypte."),
            List.of(s1)
        );

        // This quest will be automatically loaded by QuestBootstrap
        QuestRegistry.register(QUEST);
    }

    private FirePurge() {}
}

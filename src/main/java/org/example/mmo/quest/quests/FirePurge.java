package org.example.mmo.quest.quests;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.damage.DamageType;
import org.example.mmo.item.items.MATERIALS.SunkenGuardianEmber;
import org.example.mmo.quest.objectives.FetchObjective;
import org.example.mmo.quest.objectives.SlayObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;

import java.util.List;

/**
 * An example quest demonstrating the SlayObjective.
 * This quest requires defeating sunken guardians with fire damage.
 */
public final class FirePurge {
    public static final Quest QUEST;

    static {
        QuestStep s1 = new QuestStep();
        s1.name = Component.text("Purification par le Feu");
        s1.description = Component.text("Un pretre vous demande de purifier le sanctuaire en eliminant 3 gardiens engloutis par le feu.");
        s1.startNpc = "priest"; // Assume an NPC with this ID exists
        s1.endNpc = "priest";

        s1.objectives = List.of(
                new SlayObjective(
                        "sunken_guardian",
                        3,
                        "fire_purge_guardians", // Unique progress counter ID
                        Component.text("Purifier 3 gardiens engloutis par le feu"),
                        damage -> damage.getType() == DamageType.ON_FIRE
                ),
                new FetchObjective(
                        SunkenGuardianEmber.ITEM,
                        3,
                        Component.text("Ramener 3 braises englouties au pretre")
                )
        );
        s1.waitingDialogues = List.of(Component.text("Le feu est notre meilleur allie. Revenez quand les gardiens seront purifies."));
        s1.successDialogues = List.of(Component.text("Le feu a purifie leurs ames. Merci, aventurier."));

        QUEST = new Quest(
            "fire_purge",
            Component.text("Purification par le Feu"),
            Component.text("Aidez le prêtre à nettoyer la crypte."),
            List.of(s1)
        );

        // This quest will be automatically loaded by QuestBootstrap
        QuestRegistry.register(QUEST);
    }
}

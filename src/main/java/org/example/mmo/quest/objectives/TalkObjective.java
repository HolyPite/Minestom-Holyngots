package org.example.mmo.quest.objectives;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.example.data.data_class.PlayerData;
import org.example.mmo.quest.api.IQuestObjective;

import java.util.List;

public class TalkObjective implements IQuestObjective {

    private final String npcId;
    private final Component description;
    private final List<Component> dialogues;

    public TalkObjective(String npcId, Component description, List<Component> dialogues) {
        this.npcId = npcId;
        this.description = description;
        this.dialogues = dialogues;
    }

    public String getNpcId() {
        return npcId;
    }

    public List<Component> getDialogues() {
        return dialogues;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public boolean isCompleted(Player player, PlayerData data) {
        // Completion is handled by QuestProgress
        return false;
    }

    @Override
    public void onStart(Player player, PlayerData data) {}

    @Override
    public void onComplete(Player player, PlayerData data) {}

    @Override
    public void onReset(Player player, PlayerData data) {}
}

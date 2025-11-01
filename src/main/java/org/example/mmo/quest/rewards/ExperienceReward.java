package org.example.mmo.quest.rewards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import org.example.bootstrap.GameContext;
import org.example.mmo.player.data.PlayerDataService;
import org.example.mmo.quest.api.IQuestReward;

/**
 * Awards experience points to the player when the quest step completes.
 */
public final class ExperienceReward implements IQuestReward {

    private final int amount;

    public ExperienceReward(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Experience amount must be positive");
        }
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        if (player == null) {
            return;
        }
        PlayerDataService dataService = GameContext.get().playerDataService();
        dataService.grantExperience(player, amount);
    }

    @Override
    public Component getDescription() {
        return Component.text("+" + amount + " EXP", NamedTextColor.GREEN);
    }
}

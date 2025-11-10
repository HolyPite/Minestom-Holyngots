package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.example.mmo.item.GameItem;
import org.example.mmo.item.ItemRegistry;

public final class GiveItemCommand extends Command {

    private static final int MAX_STACKS_PER_COMMAND = 1024;

    public GiveItemCommand() {
        super("giveitem", "item", "gi");

        var idArg = ArgumentType.Word("id");
        idArg.setSuggestionCallback((sender, context, suggestion) ->
                ItemRegistry.all().keySet().forEach(id -> suggestion.addEntry(new SuggestionEntry(id))));

        var amountArg = ArgumentType.Integer("amount").min(1).max(MAX_STACKS_PER_COMMAND);

        setDefaultExecutor((sender, ctx) -> sender.sendMessage("Usage: /giveitem <id> [amount]"));

        var executor = (CommandExecutor) (sender, ctx) -> {
            String id = ctx.get(idArg);
            GameItem item = ItemRegistry.byId(id);

            if (item == null) {
                sender.sendMessage("Unknown item id: " + id);
                return;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by a player.");
                return;
            }
            int requested = ctx.has(amountArg) ? ctx.get(amountArg) : 1;
            int granted = giveStacks(player.getInventory(), item.toItemStack(), requested);
            if (granted < requested) {
                sender.sendMessage("Received " + granted + "x " + item.id + " (capped at " + MAX_STACKS_PER_COMMAND + ")");
            } else {
                sender.sendMessage("Received " + granted + "x " + item.id);
            }
        };

        addSyntax(executor, idArg, amountArg);
        addSyntax(executor, idArg);
    }

    private int giveStacks(PlayerInventory inventory, ItemStack template, int totalAmount) {
        int remaining = Math.min(totalAmount, MAX_STACKS_PER_COMMAND);
        int granted = remaining;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, template.maxStackSize());
            inventory.addItemStack(template.withAmount(stackAmount));
            remaining -= stackAmount;
        }
        return granted;
    }
}

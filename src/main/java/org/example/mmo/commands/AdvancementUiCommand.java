package org.example.mmo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.example.mmo.dev.advancementui.AdvancementUiService;

import java.util.StringJoiner;

/**
 * Commande utilitaire pour piloter le rendu UI des skill trees (advancements).
 */
public final class AdvancementUiCommand extends Command {

    public AdvancementUiCommand() {
        super("advui");

        var listLiteral = ArgumentType.Literal("list");
        var reloadLiteral = ArgumentType.Literal("reload");
        var openLiteral = ArgumentType.Literal("open");
        var closeLiteral = ArgumentType.Literal("close");
        var treeIdArg = ArgumentType.Word("treeId");
        var nodeIdArg = ArgumentType.Word("nodeId");
        var revealLiteral = ArgumentType.Literal("reveal");

        setDefaultExecutor((sender, ctx) ->
                sender.sendMessage("Usage: /advui list | reload | open <treeId> | close"));

        addSyntax((sender, ctx) -> {
            AdvancementUiService service = ensureService(sender);
            if (service == null) return;
            StringJoiner joiner = new StringJoiner(", ");
            service.treeIds().forEach(joiner::add);
            sender.sendMessage("Skill trees disponibles: " + (joiner.length() > 0 ? joiner : "<aucun>"));
        }, listLiteral);

        addSyntax((sender, ctx) -> {
            AdvancementUiService service = ensureService(sender);
            if (service == null) return;
            service.reload();
            sender.sendMessage("Skill trees rechargés (" + service.treeCount() + ")");
        }, reloadLiteral);

        addSyntax((sender, ctx) -> {
            AdvancementUiService service = ensureService(sender);
            if (service == null) return;
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Seuls les joueurs peuvent ouvrir un arbre.");
                return;
            }
            String treeId = ctx.get(treeIdArg);
            boolean opened = service.openTree(player, treeId);
            if (!opened) {
                sender.sendMessage("Arbre inconnu: " + treeId);
            } else {
                sender.sendMessage("Arbre '" + treeId + "' ouvert.");
            }
        }, openLiteral, treeIdArg);

        addSyntax((sender, ctx) -> {
            AdvancementUiService service = ensureService(sender);
            if (service == null) return;
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Seuls les joueurs peuvent fermer leurs arbres.");
                return;
            }
            service.closeAll(player);
            sender.sendMessage("Arbres d'advancement fermés.");
        }, closeLiteral);

        addSyntax((sender, ctx) -> {
            AdvancementUiService service = ensureService(sender);
            if (service == null) return;
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Seuls les joueurs peuvent révéler leurs arbres.");
                return;
            }
            String treeId = ctx.get(treeIdArg);
            String nodeId = ctx.get(nodeIdArg);
            if (service.revealNode(player, treeId, nodeId)) {
                sender.sendMessage("Nœud secret '" + nodeId + "' révélé.");
            } else {
                sender.sendMessage("Impossible de révéler le nœud " + nodeId + " (inexistant ou parent manquant).");
            }
        }, revealLiteral, treeIdArg, nodeIdArg);
    }

    private AdvancementUiService ensureService(net.minestom.server.command.CommandSender sender) {
        AdvancementUiService service = AdvancementUiService.get();
        if (service == null) {
            sender.sendMessage("AdvancementUiService non initialisé.");
        }
        return service;
    }
}

package org.example.bootstrap.motd;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Converts the current {@link MotdContext} into the Component sent during the
 * server list ping handshake.
 */
@FunctionalInterface
public interface MotdFormatter {

    Component format(MotdContext context);

    /**
     * Provides the default MOTD layout displayed to vanilla clients.
     *
     * @return formatter producing the stock MMO banner
     */
    static MotdFormatter defaultFormatter() {
        return context -> {
            String maxPlayers = context.maxPlayers() > 0
                    ? String.valueOf(context.maxPlayers())
                    : "illimite";

            return Component.text()
                    .append(Component.text("Holyngots MMO", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                    .append(Component.newline())
                    .append(Component.text("Entrez dans la legende.", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("Connectes: ", NamedTextColor.DARK_GREEN))
                    .append(Component.text(String.valueOf(context.onlinePlayers()), NamedTextColor.GREEN))
                    .append(Component.text("/", NamedTextColor.DARK_GREEN))
                    .append(Component.text(maxPlayers, NamedTextColor.GREEN))
                    .build();
        };
    }
}

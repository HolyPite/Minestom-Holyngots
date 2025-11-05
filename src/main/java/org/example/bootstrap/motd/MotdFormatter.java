package org.example.bootstrap.motd;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.example.utils.TKit;

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
            TextColor brandStart = TextColor.color(0xff0051);
            TextColor brandEnd = TextColor.color(0xffc200);
            TextColor eventStart = TextColor.color(0x008aff);
            TextColor eventEnd = TextColor.color(0xad00ff);

            Component firstLine = Component.text()
                    .append(Component.text(">>>>>>>>>>>> ", brandStart))
                    .append(TKit.createGradientText("HOLYNGOTS", brandStart, brandEnd)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(".mine.fun", brandEnd))
                    .append(Component.text(" <<<<<<<<<<<<", brandEnd))
                    .build();

            Component secondLine = Component.text()
                    .append(Component.text("v1.0.0", NamedTextColor.DARK_GRAY))
                    .append(Component.text(" | ", NamedTextColor.WHITE).decorate(TextDecoration.BOLD).decorate(TextDecoration.OBFUSCATED))
                    .append(TKit.createGradientText("Event: Beta Ouverte!", eventStart, eventEnd)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" | ", NamedTextColor.WHITE).decorate(TextDecoration.BOLD).decorate(TextDecoration.OBFUSCATED))
                    .append(Component.text("[Join Now]", NamedTextColor.GREEN))
                    .build();

            return Component.text()
                    .append(firstLine)
                    .append(Component.newline())
                    .append(secondLine)
                    .build();
        };
    }
}

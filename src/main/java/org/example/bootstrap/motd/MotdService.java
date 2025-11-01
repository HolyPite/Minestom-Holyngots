package org.example.bootstrap.motd;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Centralises the server list MOTD handling by listening to Minestom's ping event.
 */
public final class MotdService {

    private static final int DEFAULT_MAX_PLAYERS = 100;

    private final AtomicReference<MotdFormatter> formatterRef = new AtomicReference<>();

    private MotdService(GlobalEventHandler eventHandler, MotdFormatter formatter) {
        this.formatterRef.set(Objects.requireNonNull(formatter, "formatter"));
        eventHandler.addListener(ServerListPingEvent.class, this::handlePing);
    }

    /**
     * Registers the MOTD service on the provided event handler using the default formatter.
     *
     * @param eventHandler Minestom global event handler
     * @return active MOTD service
     */
    public static MotdService register(GlobalEventHandler eventHandler) {
        Objects.requireNonNull(eventHandler, "eventHandler");
        return new MotdService(eventHandler, MotdFormatter.defaultFormatter());
    }

    /**
     * Overrides the active formatter.
     *
     * @param formatter new formatter to use
     */
    public void setFormatter(MotdFormatter formatter) {
        this.formatterRef.set(Objects.requireNonNull(formatter, "formatter"));
    }

    private void handlePing(ServerListPingEvent event) {
        Status status = event.getStatus();
        int maxPlayers = resolveMaxPlayers(status);
        MotdContext context = new MotdContext(
                MinecraftServer.getConnectionManager().getOnlinePlayerCount(),
                maxPlayers,
                event.getPingType()
        );
        Component description = formatterRef.get().format(context);

        Status.Builder builder = status != null ? Status.builder(status) : Status.builder();
        builder.description(description).playerInfo(context.onlinePlayers(), context.maxPlayers());
        event.setStatus(builder.build());
    }

    private static int resolveMaxPlayers(Status status) {
        if (status != null && status.playerInfo() != null && status.playerInfo().maxPlayers() > 0) {
            return status.playerInfo().maxPlayers();
        }
        return DEFAULT_MAX_PLAYERS;
    }
}

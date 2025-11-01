package org.example.bootstrap.motd;

import net.minestom.server.ping.ServerListPingType;

/**
 * Context payload exposed to MOTD formatters so they can tailor the message
 * based on the current server status.
 *
 * @param onlinePlayers current number of connected players
 * @param maxPlayers    advertised player cap exposed to the ping
 * @param pingType      protocol tier used by the pinging client
 */
public record MotdContext(int onlinePlayers, int maxPlayers, ServerListPingType pingType) {
}

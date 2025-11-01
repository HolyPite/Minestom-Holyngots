package org.example.mmo.commands;

import net.minestom.server.MinecraftServer;

public final class CommandRegister {

    private CommandRegister() {
    }

    public static void init() {
        MinecraftServer.getCommandManager().register(new GiveItemCommand());
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new StopCommand());
        MinecraftServer.getCommandManager().register(new TeleportCommand());
        MinecraftServer.getCommandManager().register(new QuestsCommand());
        MinecraftServer.getCommandManager().register(new SetQuestCommand());
        MinecraftServer.getCommandManager().register(new RemoveQuestCommand());
    }
}

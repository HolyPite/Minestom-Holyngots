package org.example.commands;

import net.minestom.server.MinecraftServer;

public class CommandRegister {
    public static void init(){
        MinecraftServer.getCommandManager().register(new GiveItemCommand());
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
    };
}

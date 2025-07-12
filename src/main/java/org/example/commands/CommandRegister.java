package org.example.commands;

import net.minestom.server.MinecraftServer;
import org.example.commands.StopCommand;
import org.example.commands.TeleportCommand;

public class CommandRegister {
    public static void init(){
        MinecraftServer.getCommandManager().register(new GiveItemCommand());
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new StopCommand());
        MinecraftServer.getCommandManager().register(new TeleportCommand());
    };
}

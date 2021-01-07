package io.github.secretx33.mobstatuschange.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class Utils {
    private static ConsoleCommandSender console;

    private Utils() { }

    public static void setConsole(@NotNull ConsoleCommandSender console){
        Utils.console = console;
    }

    public static void messageConsole(@NotNull String msg){
        Preconditions.checkNotNull(console,"console variable is null");
        console.sendMessage(ChatColor.LIGHT_PURPLE + "[MobStatusChange] " + ChatColor.GRAY + msg);
    }
}

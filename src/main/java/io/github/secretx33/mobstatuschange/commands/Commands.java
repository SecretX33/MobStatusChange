package io.github.secretx33.mobstatuschange.commands;

import io.github.secretx33.mobstatuschange.config.Config;
import io.github.secretx33.mobstatuschange.config.Const;
import io.github.secretx33.mobstatuschange.entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

public class Commands implements CommandExecutor {

    private final Main plugin;

    public Commands(@NotNull Main plugin) {
        this.plugin = plugin;
        PluginCommand cmd = plugin.getCommand("msc");
        if (cmd != null) cmd.setExecutor(this);
    }

    @Override @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (strings != null && strings.length > 0) {
            for(int i=0; i < strings.length; i++){
                strings[i] = strings[i].toLowerCase(Locale.US);
            }

            switch (strings[0]) {
                case "reload":
                    if (sender.hasPermission("msc.reload")) {
                        plugin.saveDefaultConfig();
                        plugin.reloadConfig();
                        Config.refreshConfig();
                        EntityAttributes.refreshConfig();
                        sender.sendMessage(Const.CONFIGS_RELOADED);
                    }
                    break;
                case "debug":
                    if (sender.hasPermission("msc.debug")) {
                        FileConfiguration config = plugin.getConfig();
                        Config.setDebug(!Config.getDebug());
                        config.set("general.debug", Config.getDebug());
                        plugin.saveConfig();
                        sender.sendMessage(String.format(Const.DEBUG_MODE_STATE_CHANGED, (Config.getDebug()) ? "ON" : "OFF"));
                    }
                    break;
            }
        }
        return true;
    }
}

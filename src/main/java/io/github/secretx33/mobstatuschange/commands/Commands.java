/*
This file is part of MobStatusChange.

MobStatusChange is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MobStatusChange is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MobStatusChange.  If not, see <https://www.gnu.org/licenses/>.
 */
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
                        Config.reloadConfig();
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

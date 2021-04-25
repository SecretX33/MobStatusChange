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
package com.github.secretx33.mobstatuschange.commands;

import com.github.secretx33.mobstatuschange.config.Config;
import com.github.secretx33.mobstatuschange.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

@ParametersAreNonnullByDefault
public class Commands implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Config config;

    public Commands(final JavaPlugin plugin, final Config config) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(config, "config cannot be null");
        this.plugin = plugin;
        this.config = config;
        PluginCommand cmd = plugin.getCommand("msc");
        if(cmd != null) cmd.setExecutor(this);
    }

    @Override @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(strings.length == 0) return true;

        final String sub = strings[0].toLowerCase(Locale.US);
        if(sub.equals("reload") && sender.hasPermission("msc.reload")){
            config.reload();
            sender.sendMessage(Messages.CONFIGS_RELOADED);
        }
        return true;
    }
}

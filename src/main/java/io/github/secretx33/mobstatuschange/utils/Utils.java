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

package io.github.secretx33.mobstatuschange.utils;

import com.google.common.base.Preconditions;
import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Utils {
    private static Main plugin;
    private static ConsoleCommandSender console;

    private Utils() { }

    public static void consoleMessage(String msg){
        Preconditions.checkNotNull(console,"console variable is null");
        console.sendMessage(ChatColor.LIGHT_PURPLE + "[MobStatusChange] " + ChatColor.GRAY + msg);
    }

    public static void debugMessage(String msg){
        if(!Config.getDebug()) return;
        Preconditions.checkNotNull(console,"console variable is null");
        console.sendMessage(ChatColor.LIGHT_PURPLE + "[MobStatusChange] " + ChatColor.GRAY + msg);
    }

    public static void setPlugin(Main plugin) {
        Utils.plugin = plugin;
        Utils.console = plugin.getServer().getConsoleSender();
    }
}

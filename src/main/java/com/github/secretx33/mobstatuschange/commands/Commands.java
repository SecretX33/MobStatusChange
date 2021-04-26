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
import com.github.secretx33.mobstatuschange.entity.EntityAttributesManager;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

@ParametersAreNonnullByDefault
public class Commands implements CommandExecutor, TabCompleter {

    private final Config config;
    private final EntityAttributesManager attributesManager;

    public Commands(final JavaPlugin plugin, final Config config, final EntityAttributesManager attributesManager) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(config, "config cannot be null");
        checkNotNull(attributesManager, "attributesManager cannot be null");
        this.config = config;
        this.attributesManager = attributesManager;
        PluginCommand cmd = plugin.getCommand("msc");
        if(cmd != null){
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    @Override @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(strings.length == 0) return true;

        final String sub = strings[0].toLowerCase(Locale.US);
        if(sub.equals("reload") && sender.hasPermission("msc.reload")){
            config.reload();
            attributesManager.reload();
            sender.sendMessage(Messages.CONFIGS_RELOADED);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!sender.hasPermission("msc.reload")) return Collections.emptyList();
        return Collections.singletonList("reload");
    }
}

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

package io.github.secretx33.mobstatuschange.config;

import org.bukkit.ChatColor;

public class Const {
    public static final String INVALID_ENTRY_VALUE = "entry '%s' in your config file was set as " + ChatColor.RED + "%s" + ChatColor.GRAY + ", but that's an invalid value, please use a valid value and reload your configs.";
    public static final String ENTRY_HAS_NO_VALUE = "entry '%s' in your config file has no value, please use a valid value and reload your configs.";
    public static final String ENTRY_NOT_FOUND = "entry '" + ChatColor.DARK_AQUA + "%s" + ChatColor.GRAY + "' was" + ChatColor.RED + " not" + ChatColor.GRAY + " found in your config file, please fix this issue and reload your configs.";
    public static final String SECTION_NOT_FOUND = "'%s' section could not be find in your YML config file, please fix the issue or delete the file.";
    public static final String CONFIGS_RELOADED = ChatColor.LIGHT_PURPLE + "MobStatusChange" + ChatColor.GRAY + " configs reloaded and reapplied.";
    public static final String DEBUG_MODE_STATE_CHANGED = ChatColor.LIGHT_PURPLE + "MobStatusChange" + ChatColor.GRAY + " debug mode turned %s.";

    public static final double FACTOR_BETWEEN_REAL_AND_REGISTERED_TICK = 2.8;
    public static final double MIN_HP_VALUE_TO_DIE_OF_POISON = 2.0;

    public enum KilledByPoison {
        ALL("all"), PLAYERS("players"), MONSTERS("monsters"), NONE("none");

        KilledByPoison(String type) { }
    }

    public enum ValidChannels {
        CHAT("chat"), TITLE("title");

        ValidChannels(String type) { }
    }
}

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
package com.github.secretx33.mobstatuschange.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class Config {
    @NotNull private final Plugin plugin;
    @NotNull private final Logger logger;

    private final Map<String, Object> cache = new HashMap<>();

    public Config(@NotNull final Plugin plugin, @NotNull final Logger logger) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(logger, "logger cannot be null");
        this.plugin = plugin;
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String key, final T defaultValue) {
        checkNotNull(key, "key cannot be null");
        checkNotNull(defaultValue, "defaultValue cannot be null");

        final FileConfiguration config = plugin.getConfig();

        if(defaultValue.getClass().isEnum()) {
            return (T) cache.computeIfAbsent(key, k -> {
                final String value = (config.getString(key) != null) ? config.getString(key) : "";
                try {
                    return Enum.valueOf(((Enum<?>) defaultValue).getClass(), value.toUpperCase(Locale.US));
                } catch(IllegalArgumentException e) {
                    logger.severe("Error while trying to get config key '" + key + "', value passed " + value.toUpperCase(Locale.US) + " is an invalid value, please fix this entry in the config.yml and reload the configs, defaulting to " + ((Enum<?>) defaultValue).name());
                    return defaultValue;
                }
            });
        }

        return (T) cache.computeIfAbsent(key, k -> {
            final Object configEntry = plugin.getConfig().get(key);
            // If entry is not present, silent fail to default value
            if(configEntry == null) return defaultValue;
            // If the entry is present but its type is not the expected one, warn in the console
            if(!defaultValue.getClass().isAssignableFrom(configEntry.getClass())) {
                logger.severe("Error while trying to get config key '" + key + "', it was supposed to be a " + defaultValue.getClass().getSimpleName() + " but instead it was a " + configEntry.getClass().getSimpleName() + ", please fix this entry in the config.yml and reload the configs, defaulting to " + defaultValue);
                return defaultValue;
            }
            return (defaultValue.getClass() == String.class) ? color((String)configEntry) : configEntry;
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final ConfigKeys key) {
        checkNotNull(key, "key cannot be null");
        return get(key.configEntry, (T) key.defaultValue);
    }

    public String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void reload() {
        cache.clear();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }
}

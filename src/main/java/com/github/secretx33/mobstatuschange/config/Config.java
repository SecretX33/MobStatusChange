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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
    public <T> T get(String key, T defaultValue) {
        checkNotNull(key, "key cannot be null");
        checkNotNull(defaultValue, "defaultValue cannot be null");

        FileConfiguration config = plugin.getConfig();

        if(defaultValue.getClass().isEnum()) {
            final String value = (config.getString(key) != null) ? config.getString(key) : "";
            try {
                return (T) cache.computeIfAbsent(key, k -> Enum.valueOf(((Enum<?>) defaultValue).getDeclaringClass(), value));
            } catch(IllegalArgumentException e) {
                logger.severe("Error while trying to get value of '" + key + "', please fix this entry in the config.yml and reload the configs, defaulting to " + ((Enum<?>) defaultValue).name());
            }
            return defaultValue;
        }

        return (T) cache.computeIfAbsent(key, k -> {
            Object configEntry = plugin.getConfig().get(key);
            if(configEntry != null && !(defaultValue.getClass().isAssignableFrom(configEntry.getClass()))) {
                logger.severe("Error while trying to get value of '" + key + "', it was supposed to be a " + defaultValue.getClass().getSimpleName() + " but instead it was a " + configEntry.getClass().getSimpleName() + ", please fix this entry in the config.yml and reload the configs, defaulting to " + defaultValue);
                return defaultValue;
            }
            if(configEntry == null) return defaultValue;
            return configEntry;
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKeys key) {
        checkNotNull(key, "key cannot be null");
        return get(key.configEntry, (T) key.defaultValue);
    }

    public void reload() {
        cache.clear();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public enum ConfigKeys {
        ENTITY_TYPE_KILLED_BY_POISON("general.entities-killed-by-poison", KilledByPoison.ALL),
        PLAYER_DMG_MULTIPLIER_AFFECTS_MELEE_ONLY("general.atk-damage-of-player-affects-only-melee", false),
        CREEPER_EXPLOSION_INSTA_BREAK_SHIELDS("general.creeper-explosion-insta-break-shields", false),
        CREEPER_EXPLOSION_SHIELD_DAMAGE_BYPASS("general.creeper-explosion-shield-damage-bypass", 0.0),
        SHIELDBLOCK_MESSAGE_TEXT("general.message-player-after-shieldblocking-creeper-explosion.text", ""),
        SHIELDBLOCK_MESSAGE_CHANNEl("general.message-player-after-shieldblocking-creeper-explosion.channel", ValidChannels.CHAT),
        SHIELDBLOCK_TITLE_FADE_IN("general.message-player-after-shieldblocking-creeper-explosion.fade-in", 1.0),
        SHIELDBLOCK_TITLE_STAY_TIME("general.message-player-after-shieldblocking-creeper-explosion.stay-time", 3.0),
        SHIELDBLOCK_TITLE_FADE_OUT("general.message-player-after-shieldblocking-creeper-explosion.fade-out", 1.0);

        @NotNull public final String configEntry;
        @NotNull public final Object defaultValue;

        ConfigKeys(@NotNull final String configEntry, @NotNull final Object defaultValue) {
            this.configEntry = configEntry;
            this.defaultValue = defaultValue;
        }
    }
}

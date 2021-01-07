package io.github.secretx33.mobstatuschange.config;

import io.github.secretx33.mobstatuschange.config.Const.KilledByPoison;
import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Config {
    private static Main plugin;
    private static boolean playerDamageAffectMeleeOnly;
    private static KilledByPoison whoDieOfPoison = null;
    private static boolean debug = false;

    private Config() {}

    public static void refreshConfig(){
        if(plugin == null) {
            throw new NullPointerException("Plugin variable was not set yet.");
        }
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection general = null;
        if(config.isSet("general")){
            general = config.getConfigurationSection("general");
        }
        if (general == null) {
            Utils.messageConsole(String.format(Const.SECTION_NOT_FOUND,"general"));
            return;
        }

        // Parsing configs
        String field = "entities-killed-by-poison";
        if(general.isSet(field)){
            String s = general.getString(field);
            if(s != null){
                KilledByPoison whoDies = Arrays.stream(KilledByPoison.values()).filter(v -> v.name().equalsIgnoreCase(s)).findAny().orElse(null);
                if(whoDies != null) {
                    whoDieOfPoison = whoDies;
                } else {
                    Utils.messageConsole(String.format(Const.INVALID_ENTRY_VALUE, field, s));
                }
            } else Utils.messageConsole(String.format(Const.ENTRY_HAS_NO_VALUE, field));
        } else {
            Utils.messageConsole(String.format(Const.ENTRY_NOT_FOUND, field));
        }
        if(whoDieOfPoison == null) whoDieOfPoison = KilledByPoison.NONE;

        field = "atk-damage-of-player-affects-only-melee";
        playerDamageAffectMeleeOnly = general.isSet(field) && general.getBoolean(field);

        if(config.isSet("general.debug")) debug = config.getBoolean("general.debug");
    }

    public static void setPlugin(@NotNull Main p) {
        plugin = p;
        refreshConfig();
    }

    public static KilledByPoison getWhoDieOfPoison() {
        return whoDieOfPoison;
    }

    public static boolean playerDamageAffectMeleeOnly() {
        return playerDamageAffectMeleeOnly;
    }

    public static boolean getDebug(){
        return debug;
    }

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }
}

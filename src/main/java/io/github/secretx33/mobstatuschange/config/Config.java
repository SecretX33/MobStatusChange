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

import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.config.Const.KilledByPoison;
import io.github.secretx33.mobstatuschange.config.Const.ValidChannels;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Config {
    private static Main plugin;
    private static boolean playerDamageAffectMeleeOnly;
    private static KilledByPoison whoDieOfPoison = null;
    private static boolean creeperExplosionInstaBreakShields;
    private static double creeperExplosionShieldBypassPercent;
    private static String messagePlayerAfterShieldblockingCreeperExplosion = null;
    private static ValidChannels channel = ValidChannels.CHAT;
    private static int fadeIn = 0;
    private static int stayTime = 0;
    private static int fadeOut = 0;
    private static boolean debug = false;

    private Config() {}

    public static void reloadConfig(){
        if(plugin == null) {
            throw new NullPointerException("Plugin variable was not set yet.");
        }
        FileConfiguration config = plugin.getConfig();
        String section = "general";
        ConfigurationSection general = null;
        if(config.isSet(section)){
            general = config.getConfigurationSection(section);
        }
        if (general == null) {
            Utils.consoleMessage(String.format(Const.SECTION_NOT_FOUND, section));
            return;
        }

        // Parsing configs
        String field = "entities-killed-by-poison";
        whoDieOfPoison = KilledByPoison.NONE;
        if(general.isSet(field)){
            String s = general.getString(field);
            if(s != null){
                KilledByPoison whoDies = Arrays.stream(KilledByPoison.values()).filter(v -> v.name().equalsIgnoreCase(s)).findAny().orElse(null);
                if(whoDies != null) {
                    whoDieOfPoison = whoDies;
                } else {
                    Utils.consoleMessage(String.format(Const.INVALID_ENTRY_VALUE, field, s));
                }
            } else Utils.consoleMessage(String.format(Const.ENTRY_HAS_NO_VALUE, field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = "atk-damage-of-player-affects-only-melee";
        playerDamageAffectMeleeOnly = false;
        if(general.isSet(field)){
            String s = general.getString(field);
            if(s != null){
                playerDamageAffectMeleeOnly = general.getBoolean(field);
            } else
                Utils.consoleMessage(String.format(Const.ENTRY_HAS_NO_VALUE, field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = "creeper-explosion-insta-break-shields";
        creeperExplosionInstaBreakShields = false;
        if(general.isSet(field)){
            boolean b = general.getBoolean(field);
            creeperExplosionInstaBreakShields = general.getBoolean(field);
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = "creeper-explosion-shield-damage-bypass";
        creeperExplosionShieldBypassPercent = 0;
        if(general.isSet(field)){
            creeperExplosionShieldBypassPercent = Math.max(0, Math.min(1, general.getDouble(field)));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        section = "message-player-after-shieldblocking-creeper-explosion";
        if(!general.isSet(section)){
            Utils.consoleMessage(String.format(Const.SECTION_NOT_FOUND,section));
            return;
        }
        section += ".";

        field = section + "text";
        if(general.isSet(field)){
            String s = general.getString(field);
            if(s != null && !s.equalsIgnoreCase("")){
                messagePlayerAfterShieldblockingCreeperExplosion = s.replaceAll("&","§");
            } else if(s == null)
                Utils.consoleMessage(String.format(Const.ENTRY_HAS_NO_VALUE, field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = section + "channel";
        if(general.isSet(field)){
            String s = general.getString(field);
            if(s != null && !s.equalsIgnoreCase("")){
                channel = Arrays.stream(ValidChannels.values()).filter(c -> c.name().equalsIgnoreCase(s)).findAny().orElse(null);
                if(channel == null)
                    Utils.consoleMessage(String.format(Const.INVALID_ENTRY_VALUE, field, s));
            } else if(s == null)
                Utils.consoleMessage(String.format(Const.ENTRY_HAS_NO_VALUE, field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = section + "fade-in";
        if(general.isSet(field)){
            fadeIn = Math.max(0, general.getInt(field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = section + "stay-time";
        if(general.isSet(field)){
            stayTime = Math.max(0, general.getInt(field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = section + "fade-out";
        if(general.isSet(field)){
            fadeOut = Math.max(0, general.getInt(field));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        if(config.isSet("general.debug")) debug = config.getBoolean("general.debug");
    }

    public static void setPlugin(@NotNull Main p) {
        plugin = p;
    }

    public static KilledByPoison getWhoDieOfPoison() {
        return whoDieOfPoison;
    }

    public static boolean playerDamageAffectMeleeOnly() {
        return playerDamageAffectMeleeOnly;
    }

    public static boolean doesCreeperExplosionInstaBreakShields() {
        return creeperExplosionInstaBreakShields;
    }

    public static double getCreeperExplosionShieldBypassPercent() {
        return creeperExplosionShieldBypassPercent;
    }

    public static boolean shouldMsgPlayerAfterShieldblockingCreeperExplosion() {
        return messagePlayerAfterShieldblockingCreeperExplosion != null && !messagePlayerAfterShieldblockingCreeperExplosion.equals("");
    }

    public static String getMessagePlayerAfterShieldblockingCreeperExplosion() {
        return messagePlayerAfterShieldblockingCreeperExplosion;
    }

    public static ValidChannels getChannel() {
        return channel;
    }

    public static int getFadeIn() {
        return fadeIn;
    }

    public static int getStayTime() {
        return stayTime;
    }

    public static int getFadeOut() {
        return fadeOut;
    }

    public static boolean getDebug(){
        return debug;
    }

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }
}

package com.github.secretx33.mobstatuschange.config;

import org.jetbrains.annotations.NotNull;

public enum ConfigKeys {
    ENTITY_TYPE_KILLED_BY_POISON("general.entities-killed-by-poison", KilledByPoison.ALL),
    PLAYER_DMG_MULTIPLIER_AFFECTS_MELEE_ONLY("general.atk-damage-of-player-affects-only-melee", false),
    CREEPER_EXPLOSION_INSTA_BREAK_SHIELDS("general.creeper-explosion-insta-break-shields", false),
    CREEPER_EXPLOSION_SHIELD_DAMAGE_BYPASS("general.creeper-explosion-shield-damage-bypass", 0.0),
    SHIELDBLOCK_MESSAGE_TEXT("general.message-player-after-shieldblocking-creeper-explosion.text", ""),
    SHIELDBLOCK_MESSAGE_SUBTITLE("general.message-player-after-shieldblocking-creeper-explosion.subtitle", ""),
    SHIELDBLOCK_MESSAGE_CHANNEl("general.message-player-after-shieldblocking-creeper-explosion.channel", ValidChannel.CHAT),
    SHIELDBLOCK_TITLE_FADE_IN("general.message-player-after-shieldblocking-creeper-explosion.fade-in", 1.0),
    SHIELDBLOCK_TITLE_STAY_TIME("general.message-player-after-shieldblocking-creeper-explosion.stay-time", 3.0),
    SHIELDBLOCK_TITLE_FADE_OUT("general.message-player-after-shieldblocking-creeper-explosion.fade-out", 1.0);

    @NotNull
    public final String configEntry;
    @NotNull
    public final Object defaultValue;

    ConfigKeys(@NotNull final String configEntry, @NotNull final Object defaultValue) {
        this.configEntry = configEntry;
        this.defaultValue = defaultValue;
    }
}

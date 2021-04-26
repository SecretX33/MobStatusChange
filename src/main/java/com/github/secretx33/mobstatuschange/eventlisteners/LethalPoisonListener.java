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
package com.github.secretx33.mobstatuschange.eventlisteners;

import com.github.secretx33.mobstatuschange.config.Config;
import com.github.secretx33.mobstatuschange.config.ConfigKeys;
import com.github.secretx33.mobstatuschange.config.KilledByPoison;
import com.github.secretx33.mobstatuschange.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.round;

@ParametersAreNonnullByDefault
public class LethalPoisonListener implements Listener {

    private static final double MIN_HP_VALUE_TO_DIE_OF_POISON = 2.0;

    private final Plugin plugin;
    private final Config config;
    private final Set<UUID> scheduledPoisonChecks = new HashSet<>();

    public LethalPoisonListener(final Plugin plugin, final Config config) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(config, "config cannot be null");
        this.plugin = plugin;
        this.config = config;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPoisonTick(EntityDamageEvent event){
        final KilledByPoison poisonKills = config.get(ConfigKeys.ENTITY_TYPE_KILLED_BY_POISON);
        if(event.getCause() != DamageCause.POISON || poisonKills == KilledByPoison.NONE) return;

        Entity entity = event.getEntity();
        if(!(entity instanceof LivingEntity) || poisonKills == KilledByPoison.PLAYERS && !(entity instanceof Player) || poisonKills == KilledByPoison.MONSTERS && !(entity instanceof Monster)) return;

        LivingEntity livingEntity = (LivingEntity)entity;

        // if the entity HP is higher than the threshold, return
        if(livingEntity.getHealth() > MIN_HP_VALUE_TO_DIE_OF_POISON) return;
        scheduleLethalPoisonTick(livingEntity);
    }

    private void scheduleLethalPoisonTick(final LivingEntity livingEntity){
        final PotionEffect poison = getPoison(livingEntity);
        // If entity is not poisoned or the lethal tick is already scheduled
        if(poison == null || scheduledPoisonChecks.contains(livingEntity.getUniqueId())) return;

        // Entity is poisoned and have low HP
        scheduledPoisonChecks.add(livingEntity.getUniqueId());
        long poisonTickRate = getPoisonDelayBetweenTicks(poison);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!isPoisoned(livingEntity) || !scheduledPoisonChecks.contains(livingEntity.getUniqueId()) || livingEntity.getHealth() > (MIN_HP_VALUE_TO_DIE_OF_POISON + 0.5)) return;
            killByPoison(livingEntity);
            scheduledPoisonChecks.remove(livingEntity.getUniqueId());
        }, poisonTickRate);
    }

    @Nullable
    private PotionEffect getPoison(final LivingEntity livingEntity) {
        return livingEntity.getActivePotionEffects().stream().filter(effect -> effect.getType().equals(PotionEffectType.POISON)).findFirst().orElse(null);
    }

    private boolean isPoisoned(final LivingEntity livingEntity) {
        return getPoison(livingEntity) != null;
    }

    private int getPoisonDelayBetweenTicks(final PotionEffect poison){
        final int amp = poison.getAmplifier();
        double delay = 500; // delay in milliseconds

        if(amp == 0) delay = 1250;
        else if(amp <= 3) delay = 600;

        return (int) round(delay / 50) + 1;
    }

    // If player tries to logs off and he is on the scheduledPoisonChecks, he's low hp and probably logging off to avoid dying poisoned, so the plugin will kill him to prevent this exploit
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        final KilledByPoison poisonKills = config.get(ConfigKeys.ENTITY_TYPE_KILLED_BY_POISON);
        if(poisonKills == KilledByPoison.NONE || !scheduledPoisonChecks.contains(player.getUniqueId())) return;
        killByPoison(event.getPlayer());
        scheduledPoisonChecks.remove(event.getPlayer().getUniqueId());
    }

    private void killByPoison(final LivingEntity livingEntity) {
        EntityDamageEvent event = new EntityDamageEvent(livingEntity, DamageCause.POISON, 100_000);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        livingEntity.setLastDamageCause(event);
        livingEntity.damage(event.getFinalDamage());
    }
}

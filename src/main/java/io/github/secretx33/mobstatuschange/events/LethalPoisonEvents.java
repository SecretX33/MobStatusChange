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
package io.github.secretx33.mobstatuschange.events;

import io.github.secretx33.mobstatuschange.config.Config;
import io.github.secretx33.mobstatuschange.config.KilledByPoison;
import io.github.secretx33.mobstatuschange.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
public class LethalPoisonEvents implements Listener {

    private final Plugin plugin;
    private final Set<UUID> scheduledPoisonChecks = new HashSet<>();

    public LethalPoisonEvents(Plugin plugin) {
        checkNotNull(plugin);
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPoisonTick(EntityDamageEvent event){
        if(event.getCause() != EntityDamageEvent.DamageCause.POISON || Config.getWhoDieOfPoison() == KilledByPoison.NONE) return;

        Entity entity = event.getEntity();
        if(!(entity instanceof LivingEntity)) return;
        final KilledByPoison whoDies = Config.getWhoDieOfPoison();

        if(whoDies == KilledByPoison.ALL || whoDies == KilledByPoison.PLAYERS && entity instanceof Player || whoDies == KilledByPoison.MONSTERS && entity instanceof Monster) return;
        LivingEntity livingEntity = (LivingEntity)entity;

        // if the entity HP is higher than the threshold, return
        if(livingEntity.getHealth() > Messages.MIN_HP_VALUE_TO_DIE_OF_POISON) return;
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
            if(!isPoisoned(livingEntity) || !scheduledPoisonChecks.contains(livingEntity.getUniqueId()) || livingEntity.getHealth() > Messages.MIN_HP_VALUE_TO_DIE_OF_POISON) return;
            killByPoison(livingEntity);
            scheduledPoisonChecks.remove(livingEntity.getUniqueId());
        }, poisonTickRate);
    }

    @Nullable
    private PotionEffect getPoison(final LivingEntity livingEntity) {
        return livingEntity.getActivePotionEffects().stream().filter(effect -> effect.getType() == PotionEffectType.POISON).findFirst().orElse(null);
    }

    private boolean isPoisoned(final LivingEntity livingEntity) {
        return getPoison(livingEntity) != null;
    }

    private int getPoisonDelayBetweenTicks(PotionEffect poison){
        final int amp = poison.getAmplifier();
        double delay = 500; // delay in milliseconds

        if(amp == 0) delay = 1250;
        else if(amp <= 3) delay = 600;

        return (int) round(delay / 20) + 1;
    }

    // If player tries to logs off and he is on the scheduledPoisonChecks, he's low hp and probably logging off to avoid dying poisoned, so the plugin will kill him to prevent this exploit
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        if(Config.getWhoDieOfPoison() == KilledByPoison.NONE || !scheduledPoisonChecks.contains(player.getUniqueId())) return;
        killByPoison(event.getPlayer());
        scheduledPoisonChecks.remove(event.getPlayer().getUniqueId());
    }

    private void killByPoison(final LivingEntity livingEntity) {
        EntityDamageEvent event = new EntityDamageEvent(livingEntity, EntityDamageEvent.DamageCause.POISON, 100_000);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        livingEntity.setLastDamageCause(event);
        livingEntity.damage(event.getFinalDamage());
    }
}

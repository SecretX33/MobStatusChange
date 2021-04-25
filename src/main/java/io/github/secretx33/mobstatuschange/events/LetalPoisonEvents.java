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

import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.config.Config;
import io.github.secretx33.mobstatuschange.config.Const;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class LetalPoisonEvents implements Listener {

    private final Main plugin;
    private final List<UUID> scheduledPoisonChecks;

    public LetalPoisonEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        scheduledPoisonChecks = new ArrayList<>();
    }

    // Poison can kill
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    private void onDamage(EntityDamageEvent event){
        if (Config.getWhoDieOfPoison() == Const.KilledByPoison.NONE) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.POISON){
            Entity e = event.getEntity();
            final Const.KilledByPoison whoDies = Config.getWhoDieOfPoison();

            if ((whoDies.equals(Const.KilledByPoison.ALL) && e instanceof LivingEntity) ||
                    (whoDies.equals(Const.KilledByPoison.PLAYERS) && e instanceof Player) ||
                    (whoDies.equals(Const.KilledByPoison.MONSTERS) && e instanceof Monster)){
                LivingEntity le = (LivingEntity)e;
                if(le.getHealth() <= Const.MIN_HP_VALUE_TO_DIE_OF_POISON){
                    schedulePoisonCheck(le);
                }
            }
        }
    }

    private void schedulePoisonCheck(LivingEntity le){
        final PotionEffect poison = le.getActivePotionEffects().stream().filter(effect -> effect.getType().getName().equalsIgnoreCase("poison")).findFirst().orElse(null);
        // Entity is poisoned and have low HP
        if(poison != null && !scheduledPoisonChecks.contains(le.getUniqueId())){
            scheduledPoisonChecks.add(le.getUniqueId());

            final int poisonTickRate = getPoisonDelayBetweenTicks(poison);
            final UUID leUUID = le.getUniqueId();
            final WeakReference<LivingEntity> wrLe = new WeakReference<>(le);

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if(scheduledPoisonChecks.contains(leUUID) && wrLe.get() != null && !wrLe.get().isDead() && wrLe.get().getHealth() <= Const.MIN_HP_VALUE_TO_DIE_OF_POISON){
                            final boolean isPoisoned = wrLe.get().getActivePotionEffects().stream().anyMatch(effect -> effect.getType().getName().equalsIgnoreCase("poison"));
                            if(isPoisoned) wrLe.get().damage(30.0);
                        }
                    } catch(NullPointerException ignored){ /* Nope */ }
                    finally {
                        scheduledPoisonChecks.remove(leUUID);
                    }
                }
            }.runTaskLater(this.plugin, poisonTickRate);
        }
    }

    private int getPoisonDelayBetweenTicks(PotionEffect poison){
        final int amp = poison.getAmplifier();
        double delay; // delay in milliseconds

        if(amp == 0) {
            delay = 1250;
        } else if (amp <= 3){
            delay = 600;
        } else {
            delay = 500;
        }
        return (int)Math.round((delay / (plugin.getTps() * Const.FACTOR_BETWEEN_REAL_AND_REGISTERED_TICK)) + 1);
    }

    // If player tries to logs off and he is on the scheduledPoisonChecks, he's low hp and probably logging off to avoid dying poisoned, so the plugin will kill him to prevent this exploit
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    private void onPlayerQuit(PlayerQuitEvent event){
        if(Config.getWhoDieOfPoison() == Const.KilledByPoison.NONE) return;
        if(scheduledPoisonChecks.contains(event.getPlayer().getUniqueId())){
            event.getPlayer().damage(30.0);
            scheduledPoisonChecks.remove(event.getPlayer().getUniqueId());
        }
    }
}

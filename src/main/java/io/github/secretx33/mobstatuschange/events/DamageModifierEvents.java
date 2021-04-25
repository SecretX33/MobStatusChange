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
import io.github.secretx33.mobstatuschange.entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DamageModifierEvents implements Listener {

    public DamageModifierEvents(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Increase damage caused by entities by parsing the EntityDamageByEntityEvent event and altering the value there, that way it will work also with magic, fireballs, arrows, instant damage potions and explosions, not being limited to physical melee damage as Attribute.GENERIC_ATTACK_DAMAGE is
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onEntityDamagedByEntity(EntityDamageByEntityEvent event){
        modifyEntityDamage(event, event.getDamager(), event.getEntity());
    }

    private void modifyEntityDamage(EntityDamageEvent event, Entity attacker, Entity defender){
        Double dmgMod = null;

        // Defender is not a mob nor a player
        if(!(defender instanceof LivingEntity)) return;

        // Damage is from player, it's not melee damage and alterMeleePlayerDamage is ON
        if(isEntityPlayerOrFromPlayer(attacker) && Config.playerDamageAffectMeleeOnly() && !isCauseMeleeDamage(event.getCause())){
            return;
        }
        if(Config.getDebug() && defender instanceof Player) Utils.consoleMessage("Player " + defender.getName() + " took damage of " + attacker.getName());

        if(attacker instanceof Projectile){
            ProjectileSource source = ((Projectile)attacker).getShooter();
            if(source instanceof LivingEntity){
                LivingEntity shooter = ((LivingEntity)source);
                EntityAttributes attrib = EntityAttributes.getEntityAttributes(shooter);
                if(attrib != null){
                    dmgMod = attrib.getAtkDamageMod();
                }
            }
        } else {
            EntityAttributes attrib = EntityAttributes.getEntityAttributes(attacker);
            if(attrib != null){
                dmgMod = attrib.getAtkDamageMod();
            }
        }
        if(dmgMod != null){
            if(Config.getDebug() && (attacker instanceof Player || defender instanceof Player) && dmgMod != 1) Utils.consoleMessage(String.format("Damage from %s on %s before change was %s.", attacker.getName(), defender.getName(), event.getDamage()));
            event.setDamage(event.getDamage() * dmgMod);
            if(Config.getDebug() && (attacker instanceof Player || defender instanceof Player) && dmgMod != 1) Utils.consoleMessage(String.format("Damage from %s on %s after change is %s.", attacker.getName(), defender.getName(), event.getDamage()));
        }
    }

    private boolean isEntityPlayerOrFromPlayer(Entity e){
        return (e instanceof Player) || (e instanceof Projectile && ((Projectile) e).getShooter() instanceof Player);
    }

    private boolean isCauseMeleeDamage(DamageCause cause){
        return cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
    }

    // Replace any poison potion thrown by a mob that has damage mod by a stronger (or weaker) one
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPotionSplash(PotionSplashEvent event){
        ProjectileSource source = event.getEntity().getShooter();
        Double dmgMod = null;

        if(source instanceof LivingEntity){
            LivingEntity shooter = ((LivingEntity)source);
            EntityAttributes attrib = EntityAttributes.getEntityAttributes(shooter);
            if(attrib != null){
                dmgMod = attrib.getAtkDamageMod();
            }
        }

        if(dmgMod != null){
            try {
                ThrownPotion thrownPotion = event.getPotion();
                ItemStack potion = thrownPotion.getItem();
                PotionMeta potionMeta = (PotionMeta)potion.getItemMeta();
                if(potionMeta == null) return;
                PotionData potionData = potionMeta.getBasePotionData();
                if(potionData.getType() != PotionType.POISON) return;

                // Thrown potion is poison and shooter has a damage modifier
                event.setCancelled(true);
                PotionEffect potionEffect = (PotionEffect)thrownPotion.getEffects().toArray()[0];
                final int duration = potionEffect.getDuration();
                final int amplifier = (int)Math.round(potionEffect.getAmplifier() * ((dmgMod > 1) ? dmgMod*2 : dmgMod));  // Poison is too weak, so I'm boosting its damage a bit if user has increased mob's damage above baseline (1)
                final PotionEffectType type = potionEffect.getType();
                final PotionEffect pe = new PotionEffect(type, duration, Math.max(0, Math.min(amplifier, 31))); // Refer to https://minecraft.gamepedia.com/Poison#cite_note-immunity_2-2:~:text=Amplifiers%20outside%20the%20range%200%E2%80%9331%20(corresponding,or%2065%20is%20treated%20as%201).
                event.getAffectedEntities().forEach(livingEntity -> livingEntity.addPotionEffect(pe));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // Fire duration is also increased by dmgMod, since it's not possible to change fire damage directly
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityCombustByEntity(EntityCombustByEntityEvent event){
        Entity attacker = event.getCombuster();
        Entity defender = event.getEntity();
        Double dmgMod = null;

        if(!(defender instanceof LivingEntity)) return;

        // Damage is from player and playerDamageAffectMeleeOnly is ON
        if(isEntityPlayerOrFromPlayer(attacker) && Config.playerDamageAffectMeleeOnly()){
            return;
        }

        if(attacker instanceof Projectile){
            ProjectileSource source = ((Projectile)attacker).getShooter();
            if(source instanceof LivingEntity){
                LivingEntity shooter = ((LivingEntity)source);
                EntityAttributes attrib = EntityAttributes.getEntityAttributes(shooter);
                if(attrib != null){
                    dmgMod = attrib.getAtkDamageMod();
                }
            }
        } else {
            EntityAttributes attrib = EntityAttributes.getEntityAttributes(attacker);
            if(attrib != null){
                dmgMod = attrib.getAtkDamageMod();
            }
        }
        if(dmgMod != null){
            if(Config.getDebug() && defender instanceof Player && dmgMod != 1) Utils.consoleMessage(String.format("Fire duration from %s on %s before change was %s.", attacker.getName(), defender.getName(), event.getDuration()));
            event.setDuration((int)Math.round(event.getDuration() * dmgMod));
            if(Config.getDebug() && defender instanceof Player && dmgMod != 1) Utils.consoleMessage(String.format("Fire duration from %s on %s after change is %s.", attacker.getName(), defender.getName(), event.getDuration()));
        }
    }

    // Future Reflections
    /*try {
        Skeleton creeper = (Skeleton)event.getEntity();
        Class classs = creeper.getClass();
        Field[] fields = classs.getDeclaredFields();
        Utils.sendMessageToConsole(classs.getName() + " DECLARED FIELDS");
        Utils.sendMessageToConsole("----------------------");
        for (int i = 0; i < fields.length; i++) {
            Utils.sendMessageToConsole(fields[i].getType().getName() + " " + fields[i].getName());
        }
        Method[] methods = classs.getDeclaredMethods();
        Utils.sendMessageToConsole(classs.getName() + " DECLARED METHODS");
        Utils.sendMessageToConsole("----------------------");
        for (int i = 0; i < methods.length; i++) {
            Parameter[] paras = methods[i].getParameters();
            String prs = "";
            for (int j = 0; j < paras.length; j++) {
                if (j == 0) prs = "(";
                prs += paras[j].getName() + ((j + 1 < paras.length) ? ", " : ")");
            }
            Utils.sendMessageToConsole(methods[i].getName() + prs);
        }

        Method setIsDead = playerClass.getDeclaredMethod("setIsDead", (Class<?>[])null);
        Class[] methodParams = new Class[]{boolean.class};
        setIsDead.setAccessible(true);
        Object[] params = new Object[]{true};
        setIsDead.invoke(p, params);
        Utils.sendMessageToConsole("succeded changed player isDead to true");
        } catch (Exception e) {
            e.printStackTrace();
    }*/
}

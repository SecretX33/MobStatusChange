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
package com.github.secretx33.mobstatuschange.events;

import com.github.secretx33.mobstatuschange.config.Config;
import com.github.secretx33.mobstatuschange.entity.EntityAttributes;
import com.github.secretx33.mobstatuschange.entity.EntityAttributesManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.logging.Logger;

import static com.github.secretx33.mobstatuschange.config.Config.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.*;

@ParametersAreNonnullByDefault
public class DamageModifierEvents implements Listener {

    private final Logger logger;
    private final Config config;
    private final EntityAttributesManager attributesManager;

    public DamageModifierEvents(final Plugin plugin, final Logger logger, final Config config, final EntityAttributesManager attributesManager) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(logger, "logger cannot be null");
        checkNotNull(config, "config cannot be null");
        checkNotNull(attributesManager, "attributesManager cannot be null");
        this.logger = logger;
        this.config = config;
        this.attributesManager = attributesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Nullable
    private Double getDamageModifier(final Entity entity) {
        checkNotNull(entity, "entity cannot be null");

        final EntityAttributes attrib;
        if(entity instanceof Projectile){
            ProjectileSource source = ((Projectile) entity).getShooter();
            if(!(source instanceof LivingEntity)) return null;
            LivingEntity shooter = ((LivingEntity)source);
            attrib = attributesManager.getAttributesOf(shooter);
        } else {
            attrib = attributesManager.getAttributesOf(entity);
        }
        if(attrib == null) return null;
        return attrib.getAtkDamageMod();
    }

    // Increase damage caused by entities by parsing the EntityDamageByEntityEvent event and altering the value there,
    // that way it will work also with magic, fireballs, arrows, instant damage potions and explosions, not being limited
    // to physical melee damage as Attribute.GENERIC_ATTACK_DAMAGE is
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamagedByEntity(EntityDamageByEntityEvent event){
        modifyEventDamage(event, event.getDamager(), event.getEntity());
    }

    private void modifyEventDamage(EntityDamageEvent event, Entity attacker, Entity defender){
        // Defender is not a mob nor a player
        if(!(defender instanceof LivingEntity)) return;

        // Damage is from player, it's not melee damage and alterMeleePlayerDamage is ON
        if(isEntityPlayerOrFromPlayer(attacker) && (boolean)config.get(ConfigKeys.PLAYER_DMG_MULTIPLIER_AFFECTS_MELEE_ONLY) && !isCauseMeleeDamage(event.getCause()))
            return;

        if(defender instanceof Player) logger.fine("Player " + defender.getName() + " took damage of " + attacker.getName());

        final Double dmgMod = getDamageModifier(attacker);
        if(dmgMod == null || dmgMod == 1.0) return;

        if(attacker instanceof Player || defender instanceof Player) logger.fine(String.format("Damage from %s on %s before change was %s.", attacker.getName(), defender.getName(), event.getDamage()));
        event.setDamage(event.getDamage() * dmgMod);
        if(attacker instanceof Player || defender instanceof Player) logger.fine(String.format("Damage from %s on %s after change is %s.", attacker.getName(), defender.getName(), event.getDamage()));
    }

    private boolean isEntityPlayerOrFromPlayer(Entity e){
        return (e instanceof Player) || (e instanceof Projectile && ((Projectile) e).getShooter() instanceof Player);
    }

    private boolean isCauseMeleeDamage(DamageCause cause){
        return cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.ENTITY_SWEEP_ATTACK;
    }
    // Replace any poison potion thrown by a mob that has damage mod by a stronger (or weaker) one

    @EventHandler(ignoreCancelled = true)
    private void onPoisonPotionSlash(PotionSplashEvent event){
        ProjectileSource source = event.getEntity().getShooter();
        if(!(source instanceof LivingEntity)) return;
        LivingEntity shooter = ((LivingEntity)source);

        EntityAttributes attrib = attributesManager.getAttributesOf(shooter);
        if(attrib == null) return;

        Double dmgMod = attrib.getAtkDamageMod();
        if(dmgMod == null) return;

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
        final int amplifier = (int)round(potionEffect.getAmplifier() * ((dmgMod > 1) ? dmgMod*2 : dmgMod));  // Poison is too weak, so I'm boosting its damage a bit if user has increased mob's damage above baseline (1)
        final PotionEffectType type = potionEffect.getType();
        final PotionEffect pe = new PotionEffect(type, duration, max(0, min(amplifier, 31))); // Refer to https://minecraft.gamepedia.com/Poison#cite_note-immunity_2-2:~:text=Amplifiers%20outside%20the%20range%200%E2%80%9331%20(corresponding,or%2065%20is%20treated%20as%201).
        event.getAffectedEntities().forEach(livingEntity -> livingEntity.addPotionEffect(pe));
    }
    // Fire duration is also increased by dmgMod, since it's not possible to change fire damage directly

    @EventHandler(ignoreCancelled = true)
    private void onEntityCombustByEntity(EntityCombustByEntityEvent event){
        Entity attacker = event.getCombuster();
        Entity defender = event.getEntity();
        if(!(defender instanceof LivingEntity)) return;

        // Damage is from player and playerDamageAffectMeleeOnly is ON
        if(isEntityPlayerOrFromPlayer(attacker) && (boolean)config.get(ConfigKeys.PLAYER_DMG_MULTIPLIER_AFFECTS_MELEE_ONLY)) return;

        final Double dmgMod = getDamageModifier(attacker);
        if(dmgMod == null || dmgMod == 1.0) return;

        if(defender instanceof Player) logger.fine(String.format("Fire duration from %s on %s before change was %s.", attacker.getName(), defender.getName(), event.getDuration()));
        event.setDuration((int) round(event.getDuration() * dmgMod));
        if(defender instanceof Player) logger.fine(String.format("Fire duration from %s on %s after change is %s.", attacker.getName(), defender.getName(), event.getDuration()));
    }
}

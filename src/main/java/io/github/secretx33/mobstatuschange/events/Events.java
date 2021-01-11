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
import io.github.secretx33.mobstatuschange.config.Const.KilledByPoison;
import io.github.secretx33.mobstatuschange.entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

@ParametersAreNonnullByDefault
public class Events implements Listener {

    private final Main plugin;
    private FileConfiguration config;
    private final ArrayList<UUID> scheduledPoisonChecks;
    private final Logger LOG;


    public Events(Main plugin) {
        this.plugin = plugin;
        LOG = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        scheduledPoisonChecks = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent event){
        if (Config.getDebug() && (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG ||  event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            LivingEntity e = event.getEntity();
            AttributeInstance hp = e.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance followRange = e.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
            AttributeInstance atkDamage = e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            AttributeInstance atkSpeed = e.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            AttributeInstance atkKnockBack = e.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
            AttributeInstance knockBackResist = e.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            AttributeInstance movSpeed = e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            AttributeInstance flyingSpeed = e.getAttribute(Attribute.GENERIC_FLYING_SPEED);
            AttributeInstance spawnReinforcements = e.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);

            plugin.getServer().getConsoleSender().sendMessage("");
            Utils.consoleMessage(String.format("Base status of %s%s\nhp = %s (%s)\nfr = %s\ndmg = %s\natkspeed = %s\natkKnockBack = %s\nknockBackResist = %s\nmovSpeed = %s\nflyingSpeed = %s\nspawnReinforcements = %s",
                e.getName(),
                (Ageable.class.isAssignableFrom(e.getClass()) && !((Ageable)e).isAdult()) ? " (baby)" : "",
                e.getHealth(),
                hp!=null?hp.getValue():"unknown",
                followRange!=null?followRange.getBaseValue():"unknown",
                atkDamage!=null?atkDamage.getBaseValue():"unknown",
                atkSpeed!=null?atkSpeed.getBaseValue():"unknown",
                atkKnockBack!=null?atkKnockBack.getBaseValue():"unknown",
                knockBackResist!=null?knockBackResist.getBaseValue():"unknown",
                movSpeed!=null?movSpeed.getBaseValue():"unknown",
                flyingSpeed!=null?flyingSpeed.getBaseValue():"unknown",
                spawnReinforcements!=null?spawnReinforcements.getBaseValue():"unknown"));
        }

        EntityAttributes.setAttributesFor(event.getEntity());

        if (Config.getDebug() && (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER  || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            LivingEntity e = event.getEntity();
            AttributeInstance hp = e.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance followRange = e.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
            AttributeInstance atkDamage = e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            AttributeInstance atkSpeed = e.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            AttributeInstance atkKnockBack = e.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
            AttributeInstance knockBackResist = e.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            AttributeInstance movSpeed = e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            AttributeInstance flyingSpeed = e.getAttribute(Attribute.GENERIC_FLYING_SPEED);
            AttributeInstance spawnReinforcements = e.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);

            plugin.getServer().getConsoleSender().sendMessage("");
            Utils.consoleMessage(String.format("Altered status of %s%s\nhp = %s (%s)\nfr = %s\ndmg = %s\natkspeed = %s\natkKnockBack = %s\nknockBackResist = %s\nmovSpeed = %s\nflyingSpeed = %s\nspawnReinforcements = %s",
                    e.getName(),
                    (Ageable.class.isAssignableFrom(e.getClass()) && !((Ageable)e).isAdult()) ? " (baby)" : "",
                    e.getHealth(),
                    hp!=null?hp.getValue():"unknown",
                    followRange!=null?followRange.getValue():"unknown",
                    atkDamage!=null?atkDamage.getValue():"unknown",
                    atkSpeed!=null?atkSpeed.getValue():"unknown",
                    atkKnockBack!=null?atkKnockBack.getValue():"unknown",
                    knockBackResist!=null?knockBackResist.getValue():"unknown",
                    movSpeed!=null?movSpeed.getValue():"unknown",
                    flyingSpeed!=null?flyingSpeed.getValue():"unknown",
                    spawnReinforcements!=null?spawnReinforcements.getValue():"unknown"));
            Utils.consoleMessage("-----------------------\n");
        }
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
                PotionEffect potionEffect = (PotionEffect)thrownPotion.getEffects().toArray()[0];
                if(potionMeta == null) return;
                PotionData potionData = potionMeta.getBasePotionData();
                if(potionData.getType() != PotionType.POISON) return;
                event.setCancelled(true);
                final int duration = potionEffect.getDuration();
                final int amplifier = (int)Math.round(potionEffect.getAmplifier() * ((dmgMod > 1) ? dmgMod*2 : dmgMod));  // Poison is too weak, so I'm boosting its damage a bit if user has increased mob's damage above baseline (1)
                final PotionEffectType type = potionEffect.getType();
                final PotionEffect pe = new PotionEffect(type, duration, Math.max(0, Math.min(amplifier, 31))); // Refer to https://minecraft.gamepedia.com/Poison#cite_note-immunity_2-2:~:text=Amplifiers%20outside%20the%20range%200%E2%80%9331%20(corresponding,or%2065%20is%20treated%20as%201).
                event.getAffectedEntities().forEach(livingEntity -> livingEntity.addPotionEffect(pe));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // Poison can kill
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    private void onDamage(EntityDamageEvent event){
        if (Config.getWhoDieOfPoison() == KilledByPoison.NONE) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.POISON){
            Entity e = event.getEntity();
            final KilledByPoison killedByPoison = Config.getWhoDieOfPoison();

            if ((killedByPoison.equals(KilledByPoison.ALL) && e instanceof LivingEntity) ||
                (killedByPoison.equals(KilledByPoison.PLAYERS) && e instanceof Player) ||
                (killedByPoison.equals(KilledByPoison.MONSTERS) && e instanceof Monster)){
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
        if (Config.getWhoDieOfPoison() == KilledByPoison.NONE) return;
        if(scheduledPoisonChecks.contains(event.getPlayer().getUniqueId())){
            event.getPlayer().damage(30.0);
            scheduledPoisonChecks.remove(event.getPlayer().getUniqueId());
        }
    }

    private boolean isEntityPlayerOrFromPlayer(Entity e){
        return (e instanceof Player) || (e instanceof Projectile && ((Projectile) e).getShooter() instanceof Player);
    }

    private boolean isCauseMeleeDamage(DamageCause cause){
        return cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
    }

    // Fire duration is also increased by dmgMod, since it's not possible to change fire damage directly
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityCombustByEntity(EntityCombustByEntityEvent event){
        Entity attacker = event.getCombuster();
        Entity defender = event.getEntity();
        Double dmgMod = null;

        if(!(defender instanceof LivingEntity)) return;

        // Damage is from player and alterMeleePlayerDamage is ON
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

    // Increase damage caused by entities by parsing the EntityDamageByEntityEvent event and altering the value there, that way it will work also with magic, fireballs, arrows, instant damage potions and explosions, not being limited to physical melee damage as Attribute.GENERIC_ATTACK_DAMAGE is
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityDamaged(EntityDamageByEntityEvent event){
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

        if(Config.getDebug() && defender instanceof Player){
            Utils.consoleMessage("Player " + defender.getName() + " took damage of " + attacker.getName());
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
            if(Config.getDebug() && (attacker instanceof Player || defender instanceof Player) && dmgMod != 1) Utils.consoleMessage(String.format("Damage from %s on %s before change was %s.", attacker.getName(), defender.getName(), event.getDamage()));
            event.setDamage(event.getDamage() * dmgMod);

            if(attacker instanceof Creeper && defender instanceof Player){
                final Player p = (Player)defender;
                Utils.debugMessage(String.format("Player %s took damage of creeper.", p.getName()));
                boolean isBlocking = p.isBlocking();
                if(isBlocking){
                    // Creeper explosion insta break shield
                    if(Config.doesCreeperExplosionInstaBreakShields()){
                        Utils.debugMessage(String.format("Player %s was blocking, breaking his shield.", p.getName()));
                        ItemStack mainHand = p.getInventory().getItemInMainHand();
                        ItemStack offHand = p.getInventory().getItemInOffHand();

                        if(mainHand.getType() == Material.matchMaterial("shield")){
                            p.getInventory().setItemInMainHand(null);
                        } else if(offHand.getType() == Material.matchMaterial("shield")){
                            p.getInventory().setItemInOffHand(null);
                        }
                        p.updateInventory();
                        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                    }
                    // Creeper explosion shield damage bypass
                    final double damageBypass = Config.getCreeperExplosionShieldBypassPercent();
                    if(damageBypass > 0){
                        p.damage(event.getDamage() * damageBypass);
                        Utils.debugMessage(p.getName() + " took " + damageBypass*100 + "% of creeper explosion (creeper explosion damage bypass).");
                    }
                    //Send player a message after shieldblocking creeper explosion
                    if(Config.shouldMsgPlayerAfterShieldblockingCreeperExplosion()){
                        p.sendTitle("",Config.getMessagePlayerAfterShieldblockingCreeperExplosion(),1 * plugin.getTps(),4 * plugin.getTps(),1 * plugin.getTps());
//                        p.sendMessage();
                    }
                }
            }
            if(Config.getDebug() && (attacker instanceof Player || defender instanceof Player) && dmgMod != 1) Utils.consoleMessage(String.format("Damage from %s on %s after change is %s.", attacker.getName(), defender.getName(), event.getDamage()));
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

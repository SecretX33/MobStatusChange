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
package io.github.secretx33.mobstatuschange.Events;

import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.Config.Config;
import io.github.secretx33.mobstatuschange.Entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ApplyCustomAttribEvents implements Listener {

    private final Main plugin;

    public ApplyCustomAttribEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
}

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

import com.github.secretx33.mobstatuschange.entity.EntityAttributesManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@ParametersAreNonnullByDefault
public class ApplyCustomAttribListener implements Listener {

    private final Logger logger;
    private final EntityAttributesManager attributesManager;

    public ApplyCustomAttribListener(final Plugin plugin, final Logger logger, final EntityAttributesManager attributesManager) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(logger, "logger cannot be null");
        checkNotNull(attributesManager, "attributesManager cannot be null");
        this.logger = logger;
        this.attributesManager = attributesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent event){
        printEntityStats(event, "Base");
        attributesManager.setAttributesFor(event.getEntity());
        printEntityStats(event, "Altered");
    }

    private void printEntityStats(CreatureSpawnEvent event, String identifier) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {

            LivingEntity entity                   = event.getEntity();
            AttributeInstance hp                  = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance followRange         = entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
            AttributeInstance atkDamage           = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            AttributeInstance atkSpeed            = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            AttributeInstance atkKnockBack        = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
            AttributeInstance knockBackResist     = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            AttributeInstance movSpeed            = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            AttributeInstance flyingSpeed         = entity.getAttribute(Attribute.GENERIC_FLYING_SPEED);
            AttributeInstance spawnReinforcements = entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);

            logger.finest(String.format("\n%s status of %s%s\nhp = %s (%s)\nfr = %s\ndmg = %s\natkspeed = %s\natkKnockBack = %s\nknockBackResist = %s\nmovSpeed = %s\nflyingSpeed = %s\nspawnReinforcements = %s",
                    identifier,
                    entity.getName(),
                    (Ageable.class.isAssignableFrom(entity.getClass()) && !((Ageable)entity).isAdult()) ? " (baby)" : "",
                    entity.getHealth(),
                    hp != null                  ? hp.getValue()                      : "unknown",
                    followRange != null         ? followRange.getBaseValue()         : "unknown",
                    atkDamage != null           ? atkDamage.getBaseValue()           : "unknown",
                    atkSpeed != null            ? atkSpeed.getBaseValue()            : "unknown",
                    atkKnockBack != null        ? atkKnockBack.getBaseValue()        : "unknown",
                    knockBackResist != null     ? knockBackResist.getBaseValue()     : "unknown",
                    movSpeed != null            ? movSpeed.getBaseValue()            : "unknown",
                    flyingSpeed != null         ? flyingSpeed.getBaseValue()         : "unknown",
                    spawnReinforcements != null ? spawnReinforcements.getBaseValue() : "unknown"));
        }
    }
}

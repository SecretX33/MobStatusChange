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
import com.github.secretx33.mobstatuschange.config.ValidChannel;
import com.github.secretx33.mobstatuschange.entity.EntityAttributes;
import com.github.secretx33.mobstatuschange.entity.EntityAttributesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

@ParametersAreNonnullByDefault
public class CreeperExplosionListener implements Listener {

    private final Logger logger;
    private final Config config;
    private final EntityAttributesManager attributesManager;

    public CreeperExplosionListener(final Plugin plugin, final Logger logger, final Config config, final EntityAttributesManager attributesManager) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(logger, "logger cannot be null");
        checkNotNull(config, "config cannot be null");
        checkNotNull(attributesManager, "attributesManager cannot be null");
        this.logger = logger;
        this.config = config;
        this.attributesManager = attributesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShieldDamage(PlayerItemDamageEvent event) {
        if(event.getItem().getType() != Material.SHIELD || !(boolean)config.get(ConfigKeys.CREEPER_EXPLOSION_INSTA_BREAK_SHIELDS)) return;
        // Cancel the event to mitigate the damage on the shield, assuming the player got more than 1 shield on the stack, because we are already removing 1 of his shields
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShieldBlockCreeperExplosion(EntityDamageByEntityEvent event){
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();

        // Defender is not a Player or Attacker is not a Creeper
        if(!(defender instanceof Player) || !(attacker instanceof Creeper)) return;

        EntityAttributes attrib = attributesManager.getAttributesOf(attacker);
        if(attrib == null) return;
        Double dmgMod = attrib.getAtkDamageMod();
        if(dmgMod == null) return;

        final Player p = (Player)defender;
        boolean isBlocking = p.isBlocking();
        if(!isBlocking) return;

        // Creeper explosion insta break shield
        if(config.get(ConfigKeys.CREEPER_EXPLOSION_INSTA_BREAK_SHIELDS)){
            logger.fine(String.format("Player %s was blocking, breaking his shield.", p.getName()));
            ItemStack mainHand = p.getInventory().getItemInMainHand();
            ItemStack offHand = p.getInventory().getItemInOffHand();

            if(mainHand.getType() == Material.matchMaterial("shield")){
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else if(offHand.getType() == Material.matchMaterial("shield")){
                offHand.setAmount(offHand.getAmount() - 1);
            }
            p.updateInventory();
            p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
        }

        // Creeper explosion shield damage bypass
        double percentageBypass = max(0.0, min(1.0, config.get(ConfigKeys.CREEPER_EXPLOSION_SHIELD_DAMAGE_BYPASS)));
        if(percentageBypass > 0.0){
            p.damage(event.getDamage() * percentageBypass);
            logger.finer(p.getName() + " took " + percentageBypass * 100 + "% of creeper explosion (creeper explosion damage bypass).");
        }

        //Send player a message after shield blocking creeper explosion
        final String message = config.get(ConfigKeys.SHIELDBLOCK_MESSAGE_TEXT);
        final String subtitle = config.get(ConfigKeys.SHIELDBLOCK_MESSAGE_SUBTITLE);
        final ValidChannel channel = config.get(ConfigKeys.SHIELDBLOCK_MESSAGE_CHANNEl);
        if(channel == ValidChannel.CHAT && message.isEmpty() || channel == ValidChannel.TITLE && message.isEmpty() && subtitle.isEmpty()) return;

        if(channel == ValidChannel.CHAT){
            p.sendMessage(message);
        } else {
            final int fadeIn   = (int)((double)config.get(ConfigKeys.SHIELDBLOCK_TITLE_FADE_IN)   * 20.0);
            final int stayTime = (int)((double)config.get(ConfigKeys.SHIELDBLOCK_TITLE_STAY_TIME) * 20.0);
            final int fadeOut  = (int)((double)config.get(ConfigKeys.SHIELDBLOCK_TITLE_FADE_OUT)  * 20.0);
            p.sendTitle(message, subtitle, fadeIn, stayTime, fadeOut);
        }
    }
}

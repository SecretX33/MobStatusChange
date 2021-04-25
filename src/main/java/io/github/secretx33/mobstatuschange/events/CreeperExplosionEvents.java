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
import io.github.secretx33.mobstatuschange.entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CreeperExplosionEvents implements Listener {

    private final Main plugin;

    public CreeperExplosionEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityDamaged(EntityDamageByEntityEvent event){
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        Double dmgMod = null;

        // Defender is not a Player or Attacker is not a Creeper
        if(!(defender instanceof Player) || !(attacker instanceof Creeper)) return;

        EntityAttributes attrib = EntityAttributes.getEntityAttributes(attacker);
        if(attrib != null){
            dmgMod = attrib.getAtkDamageMod();
        }

        if(dmgMod != null){
            final Player p = (Player)defender;
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
                    String msg = Config.getMessagePlayerAfterShieldblockingCreeperExplosion();

                    if(Config.getChannel() == Const.ValidChannels.CHAT){
                        p.sendMessage(msg);
                    } else if(Config.getChannel() == Const.ValidChannels.TITLE){
                        final int tps      = plugin.getTps();
                        final int fadeIn   = Config.getFadeIn() * tps;
                        final int stayTime = Config.getStayTime() * tps;
                        final int fadeOut  = Config.getFadeOut() * tps;
                        p.sendTitle("", msg, fadeIn, stayTime, fadeOut);
                    }
                }
            }
        }
    }
}

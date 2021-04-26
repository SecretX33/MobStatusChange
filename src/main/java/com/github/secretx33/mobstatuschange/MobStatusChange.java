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
package com.github.secretx33.mobstatuschange;

import com.github.secretx33.mobstatuschange.commands.Commands;
import com.github.secretx33.mobstatuschange.config.Config;
import com.github.secretx33.mobstatuschange.entity.EntityAttributesManager;
import com.github.secretx33.mobstatuschange.eventlisteners.ApplyCustomAttribListener;
import com.github.secretx33.mobstatuschange.eventlisteners.CreeperExplosionListener;
import com.github.secretx33.mobstatuschange.eventlisteners.DamageModifierListener;
import com.github.secretx33.mobstatuschange.eventlisteners.LethalPoisonListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MobStatusChange extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final Config config = new Config(this, getLogger());
        final EntityAttributesManager attributesManager = new EntityAttributesManager(this, getLogger());
        new ApplyCustomAttribListener(this, getLogger(), attributesManager);
        new DamageModifierListener(this, getLogger(), config, attributesManager);
        new CreeperExplosionListener(this, getLogger(), config, attributesManager);
        new LethalPoisonListener(this, config);
        new Commands(this, config, attributesManager);
        getLogger().info("loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("disabled.");
    }
}

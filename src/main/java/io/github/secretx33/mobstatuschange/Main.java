package io.github.secretx33.mobstatuschange;

import io.github.secretx33.mobstatuschange.commands.Commands;
import io.github.secretx33.mobstatuschange.config.Config;
import io.github.secretx33.mobstatuschange.entity.EntityAttributes;
import io.github.secretx33.mobstatuschange.events.Events;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private int tps = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Utils.setConsole(getServer().getConsoleSender());
        Config.setPlugin(this);
        EntityAttributes.setPlugin(this);
        Commands cmds = new Commands(this);
        Events e = new Events(this);

        // Measuring TPS
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            long sec;
            long currentSec;
            int ticks;
            int delay;

            @Override
            public void run() {
                sec = (System.currentTimeMillis() / 1000);

                if (currentSec == sec) { // this code block triggers each tick
                    ticks++;
                } else { // this code block triggers each second
                    currentSec = sec;
                    tps = (tps == 0 ? ticks : ((tps + ticks) / 2));
                    ticks = 0;

                    if ((++delay % 300) == 0) {// this code block triggers each 5 minutes
                        delay = 0;
                    }
                }
            }
        }, 0, 1); // do not change the "1" value, the other one is just initial delay, I recommend 0 = start instantly.
        Utils.messageConsole("loaded.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        Utils.messageConsole("disabled.");
    }

    public int getTps() {
        return tps;
    }
}

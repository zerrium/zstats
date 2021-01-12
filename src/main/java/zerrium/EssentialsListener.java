package zerrium;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {
    @EventHandler
    public void onPlayerAfkToggle(AfkStatusChangeEvent event){
        if(Zstats.hasEssentials){
            IUser p = event.getAffected();
            if (!event.getValue()) { //back from AFK
                long x = (System.currentTimeMillis() - p.getAfkSince())/1000; //AFK time in seconds
                ZPlayer zp = Zstats.zplayer.get(Zstats.zplayer.indexOf(new ZPlayer(p.getBase().getUniqueId())));
                zp.afk_time += x;
                if(Zstats.debug) System.out.println("[Zstats] " + p.getName() + " has been AFK for " + x +" seconds.");
            }
        }
    }
}

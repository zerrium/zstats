package zerrium.listeners;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zerrium.Zstats;
import zerrium.models.ZstatsPlayer;
import zerrium.configs.ZstatsConfigs;
import zerrium.utils.ZstatsGeneralUtils;

import java.util.ArrayList;

public class ZstatsEssentialsListener implements Listener {
    @EventHandler
    public void onPlayerAfkToggle(AfkStatusChangeEvent event){
        final ArrayList<ZstatsPlayer> zplayer = ZstatsGeneralUtils.getZplayer();
        if(Zstats.getHasEssentials()){
            IUser p = event.getAffected();
            if (!event.getValue()) { //back from AFK
                long x = (System.currentTimeMillis() - p.getAfkSince())/1000; //AFK time in seconds
                ZstatsPlayer zp = zplayer.get(zplayer.indexOf(new ZstatsPlayer(p.getBase().getUniqueId())));
                zp.afk_time += x;
                if(ZstatsConfigs.getDebug()) System.out.println("[Zstats] " + p.getName() + " has been AFK for " + x +" seconds.");
            }
        }
    }
}

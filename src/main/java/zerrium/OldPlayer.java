package zerrium;

import org.bukkit.entity.Player;

import java.util.UUID;

//This class is for <1.15 Player instance where we save that Online Player instance to this class as we can't update the stats when the player is offline
public class OldPlayer {
    private final Player p;
    private final UUID uuid;

    public OldPlayer(Player p){
        this.p = p;
        this.uuid = p.getUniqueId();
    }

    public OldPlayer(UUID uuid){
        this.p = null;
        this.uuid = uuid;
    }

    public Player getPlayer(){
        return p;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            //if(Zstats.debug) System.out.println("Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof OldPlayer)) {
            //if(Zstats.debug) System.out.println("Not a OldPlayer instance");
            return false;
        }

        // Compare the data members and return accordingly
        //if(Zstats.debug) System.out.println("OldPlayer instance, equal? "+result);
        return ((OldPlayer) o).uuid.toString().equals(uuid.toString()) || uuid.toString().equals(((OldPlayer) o).uuid.toString());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}

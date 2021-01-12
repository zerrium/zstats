package zerrium;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ZPlayer {
    String name;
    UUID uuid;
    long afk_time, last_played;
    boolean is_updating; //flag to prevent double update to the same Object simultaneously
    HashMap<String, Long> x; //convert those stupid many attributes into a hashmap
    LinkedHashMap<Material, Long> craft;
    LinkedHashMap<Material, Long> place;
    LinkedHashMap<Material, Long> mine;
    LinkedHashMap<EntityType, Long> slain;
    LinkedHashMap<EntityType, Long> mob;

    public ZPlayer(UUID uuid, String name) throws SQLException {
        this.uuid = uuid;
        this.name = name;

        Connection connection = SqlCon.openConnection();
        PreparedStatement pss = null;
        ResultSet rs = null;

        if(Zstats.zstats.get("z:afk_time")){
            if(Zstats.debug) System.out.println("Get player AFK time from db");
            pss = connection.prepareStatement("select val from stats where uuid=? and stat=?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:afk_time");
            rs = pss.executeQuery();
            if (!rs.next()) {
                if(Zstats.debug) System.out.println("AFK stat Not found. set it to 0");
                afk_time = 0L;
            }else{
                afk_time = rs.getLong(1);
                if(Zstats.debug) System.out.println("AFK value: " + afk_time);
            }
        }

        if(Zstats.zstats.get("z:last_played")){
            if(Zstats.debug) System.out.println("Get player last played time from db");
            pss = connection.prepareStatement("select val from stats where uuid=? and stat=?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:last_played");
            rs = pss.executeQuery();
            if (!rs.next()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if(Zstats.debug) System.out.println("Last played stat Not found. set it to OfflinePlayer#getLastPlayed");
                last_played = p.getLastPlayed()/1000;
            }else{
                last_played = rs.getLong(1);
                if(Zstats.debug) System.out.println("Last played value: " + last_played);
            }
        }

        assert pss != null;
        pss.close();
        rs.close();
        connection.close();
        this.is_updating = false;
    }

    public ZPlayer(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            if(Zstats.debug) System.out.println("Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ZPlayer)) {
            if(Zstats.debug) System.out.println("Not a ZPlayer instance");
            return false;
        }

        // Compare the data members and return accordingly
        boolean result = ((ZPlayer) o).uuid.toString().equals(uuid.toString()) || uuid.toString().equals(((ZPlayer) o).uuid.toString());
        if(Zstats.debug) System.out.println("ZPlayer instance, equal? "+result);
        return result;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    private void clearStat(){
        this.x = new HashMap<>();
        for(Map.Entry<String, Boolean> st:Zstats.vanilla_stats.entrySet()){
            if(st.getValue()) x.put(st.getKey(), 0L);
        }
        for(Map.Entry<String, Boolean> st:Zstats.zstats.entrySet()){
            if(st.getValue()) x.put(st.getKey(), 0L);
        }
        this.craft = new LinkedHashMap<>();
        this.place = new LinkedHashMap<>();
        this.mine = new LinkedHashMap<>();
        this.slain = new LinkedHashMap<>();
        this.mob = new LinkedHashMap<>();

    }

    public void updateStat(Connection connection) throws SQLException { //Should be called Asynchronously
        this.is_updating = true;
        //Clear existing Stats
        this.clearStat();

        //Rewrite with the latest stats
        OfflinePlayer p = Bukkit.getOfflinePlayer(this.uuid);
        for(Map.Entry<String, Long> me:x.entrySet()){
            String k = me.getKey();
            if(!k.contains("z:")){
                if(Zstats.version < 5 && p.isOnline()) this.x.put(k, (long) Objects.requireNonNull(p.getPlayer()).getStatistic(Statistic.valueOf(k)));
                else if(Zstats.version >= 5) this.x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
                else return;
            }else if(k.equals("z:last_played")){
                this.x.put(k, last_played);
            }
        }

        //server world save size
        Zstats.updateWorldSize();

        //substats
        Substats s = new Substats(this);
        s.substats_Material();
        s.substats_Entity();
        s.sort_substats();

        //Update to SQL
        //World Size
        if(Zstats.zstats.get("z:world_size")) this.SQL_query(connection, Zstats.world_size, "000", "z:world_size");

        //Nether Size
        if(Zstats.zstats.get("z:nether_size")) this.SQL_query(connection, Zstats.nether_size, "000", "z:nether_size");

        //The End Size
        if(Zstats.zstats.get("z:end_size")) this.SQL_query(connection, Zstats.end_size, "000", "z:end_size");

        //Total Size
        if(Zstats.zstats.get("z:total_size")) this.SQL_query(connection, Zstats.total_size, "000", "z:total_size");

        //AFK time
        if(Zstats.zstats.get("z:afk_time")) this.SQL_query(connection, afk_time, uuid.toString(), "z:afk_time");

        //General stats
        for(Map.Entry<String, Long> me:this.x.entrySet()){
            String k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), k);
        }

        //Crafting stats
        int j = 1;
        for(Map.Entry<Material, Long> me:this.craft.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), "z:craft_", k.toString(), j);
            j++;
        }

        //Placed items/blocks stats
        j = 1;
        for(Map.Entry<Material, Long> me:this.place.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), "z:place_", k.toString(), j);
            j++;
        }

        //Mined blocks stats
        j = 1;
        for(Map.Entry<Material, Long> me:this.mine.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), "z:mine_", k.toString(), j);
            j++;
        }

        //Killing stats
        j = 1;
        for(Map.Entry<EntityType, Long> me:this.mob.entrySet()){
            EntityType k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), "z:mob_", k.toString(), j);
            j++;
        }

        //Slain stats
        j = 1;
        for(Map.Entry<EntityType, Long> me:this.slain.entrySet()){
            EntityType k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), "z:slain_", k.toString(), j);
            j++;
        }

        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Update stats of " + uuid.toString() + " associates with " + name + " done.");
        this.is_updating = false;
    }

    public void deleteStat(Connection connection) throws SQLException { //Should be called Asynchronously
        //delete from SQL
        if(this.is_updating) return;
        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Deleting stats of " + uuid.toString() + " associates with " + name + " from database...");
        PreparedStatement pss = connection.prepareStatement("delete from stats where uuid=?");
        pss.setString(1, uuid.toString());
        int row = pss.executeUpdate();
        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET +
                (row > 0 ? " Deleted stats of " + uuid.toString() + " associates with " + name + " from database." : " No stats of "  + uuid.toString() + " associates with " + name + " found on the database. No rows affected."));
        pss.close();
    }

    private void SQL_query(Connection connection, long val, String uuid, String stat) throws SQLException{ //For general stats
        PreparedStatement pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, uuid);
        pss.setString(2, stat);
        ResultSet rs = pss.executeQuery();
        PreparedStatement ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, val);
        ps.setString(2, uuid);
        ps.setString(3, stat);
        if (Zstats.debug) System.out.println(uuid + " - " + stat + " - " + val);
        ps.executeUpdate();
        pss.close();
        rs.close();
        ps.close();
    }

    private void SQL_query(Connection connection, long val, String uuid, String stat, String substat, int j) throws SQLException{ //For substats
        PreparedStatement pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
        pss.setString(1, uuid);
        pss.setString(2, stat + String.format("%04d", j) + "_%");
        pss.executeUpdate();
        PreparedStatement ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, val);
        ps.setString(2, uuid);
        ps.setString(3, stat + String.format("%04d", j) + "_" + substat);
        ps.execute();
        if (Zstats.debug) System.out.println(uuid + " - " + stat + String.format("%04d", j) + "_" + substat + " - " + val);
        pss.close();
        ps.close();
    }
}
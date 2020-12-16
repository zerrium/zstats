package zerrium;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
        if(Zstats.debug) System.out.println("Get player AFK time from db");
        PreparedStatement pss = connection.prepareStatement("select val from stats where uuid=? and stat=?");
        pss.setString(1, uuid.toString());
        pss.setString(2, "z:afk_time");
        ResultSet rs = pss.executeQuery();
        if (!rs.next()) {
            if(Zstats.debug) System.out.println("AFK stat Not found. set it to 0");
            afk_time = 0L;
        }else{
            afk_time = rs.getLong(1);
            if(Zstats.debug) System.out.println("AFK value: " + afk_time);
        }

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
        if(Zstats.debug) System.out.println("ZPlayer#clearStat");
        this.x = new HashMap<>();
        this.craft = new LinkedHashMap<>();
        this.place = new LinkedHashMap<>();
        this.mine = new LinkedHashMap<>();
        this.slain = new LinkedHashMap<>();
        this.mob = new LinkedHashMap<>();
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1");
        this.x.put(Statistic.PLAY_ONE_TICK.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-1");
        this.x.put(Statistic.DAMAGE_DEALT.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-2");
        this.x.put(Statistic.DAMAGE_TAKEN.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-3");
        this.x.put(Statistic.MOB_KILLS.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-4");
        this.x.put(Statistic.DEATHS.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-5");
        this.x.put(Statistic.SPRINT_ONE_CM.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-6");
        this.x.put(Statistic.WALK_ONE_CM.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-7");
        this.x.put(Statistic.CROUCH_ONE_CM.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-8");
        this.x.put(Statistic.BOAT_ONE_CM.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-9");
        this.x.put(Statistic.AVIATE_ONE_CM.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-10");
        this.x.put(Statistic.TRADED_WITH_VILLAGER.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-11");
        this.x.put(Statistic.TALKED_TO_VILLAGER.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-12");
        this.x.put(Statistic.CHEST_OPENED.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-13");
        this.x.put(Statistic.FISH_CAUGHT.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-14");
        this.x.put(Statistic.ITEM_ENCHANTED.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 1-15");
        this.x.put(Statistic.SLEEP_IN_BED.toString(), 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 2");
        this.x.put("z:crafted", 0L);
        this.x.put("z:mined", 0L);
        this.x.put("z:pickaxe", 0L);
        this.x.put("z:axe", 0L);
        this.x.put("z:shovel", 0L);
        this.x.put("z:hoe", 0L);
        this.x.put("z:sword", 0L);
        this.x.put("z:bow", 0L);
        this.x.put("z:placed", 0L);
        this.x.put("z:craft_kind", 0L);
        this.x.put("z:mine_kind", 0L);
        this.x.put("z:place_kind", 0L);
        this.x.put("z:mob_kind", 0L);
        this.x.put("z:slain_kind", 0L);
        this.x.put("z:last_played", 0L);
        if(Zstats.debug) System.out.println("ZPlayer#clearStat 3");
    }

    public void updateStat(Connection connection, Player p) throws SQLException, NullPointerException, IllegalArgumentException { //Should be called Asynchronously
        if(Zstats.debug) System.out.println("ZPlayer#updateStat");
        if(!p.getUniqueId().equals(this.uuid)) return;
        this.is_updating = true;
        if(Zstats.debug) System.out.println("ZPlayer#updateStat clear");
        //Clear existing Stats
        this.clearStat();

        if(Zstats.debug) System.out.println("ZPlayer#updateStat rewrite try 3");
        //Rewrite with the latest stats
        for(Map.Entry<String, Long> me:x.entrySet()){
            String k = me.getKey();
            if(!k.contains("z:")){
                this.x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
            }else if(k.equals("z:last_played")){
                this.x.put(k, last_played);
            }
        }

        if(Zstats.debug) System.out.println("ZPlayer#updateStat: world size");
        //server world save size
        Zstats.updateWorldSize();

        if(Zstats.debug) System.out.println("ZPlayer#updateStat substat");
        //substats
        Substats s = new Substats(this, p);
        s.substats_Material();
        s.substats_Entity();
        s.sort_substats();

        if(Zstats.debug) System.out.println("ZPlayer#updateStat SQL begin");
        //Update to SQL
        //World Size
        this.SQL_query(connection, Zstats.world_size, "000", "z:world_size");

        //Nether Size
        this.SQL_query(connection, Zstats.nether_size, "000", "z:nether_size");

        //The End Size
        this.SQL_query(connection, Zstats.end_size, "000", "z:end_size");

        //Total Size
        this.SQL_query(connection, Zstats.total_size, "000", "z:total_size");

        //AFK time
        this.SQL_query(connection, afk_time, uuid.toString(), "z:afk_time");

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
        pss.setString(2, stat + j + "_%");
        pss.executeUpdate();
        PreparedStatement ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, val);
        ps.setString(2, uuid);
        ps.setString(3, stat + j + "_" + substat);
        ps.execute();
        if (Zstats.debug) System.out.println(uuid + " - " + stat + j + "_" + substat + " - " + val);
        pss.close();
        ps.close();
    }
}
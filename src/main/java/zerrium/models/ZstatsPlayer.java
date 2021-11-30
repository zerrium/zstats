package zerrium.models;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import zerrium.Zstats;
import zerrium.configs.ZstatsConfigs;
import zerrium.utils.ZstatsGeneralUtils;
import zerrium.utils.ZstatsSqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;


public class ZstatsPlayer {
    public static final ArrayList<ZstatsOldPlayer> players = new ArrayList<>(); //This class is for <1.15 Player instance where we save that Online Player instance to this class as we can't update the stats when the player is offline
    private static final Logger log = Zstats.getPlugin(Zstats.class).getLogger();

    public String name;
    public final UUID uuid;
    public long afk_time, last_played;
    public boolean is_updating; //flag to prevent double update to the same Object simultaneously
    public HashMap<String, Long> x; //convert those stupid many attributes into a hashmap
    public LinkedHashMap<Material, Long> craft;
    public LinkedHashMap<Material, Long> place;
    public LinkedHashMap<Material, Long> mine;
    public LinkedHashMap<EntityType, Long> slain;
    public LinkedHashMap<EntityType, Long> mob;

    public ZstatsPlayer(UUID uuid, String name) throws SQLException {
        final HashMap<String, Boolean> zstats = ZstatsConfigs.getZstats();

        this.uuid = uuid;
        this.name = name;

        Connection connection = ZstatsSqlUtil.openConnection();
        PreparedStatement pss = null;
        ResultSet rs = null;

        if(zstats.get("z:afk_time")){
            log.fine("[Zstats: "+this.getClass().toString()+"] "+"Get player AFK time from db");
            pss = connection.prepareStatement(ZstatsSqlUtil.getTableName("select val from <$zstats> where uuid=? and stat=?"));
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:afk_time");
            rs = pss.executeQuery();
            if (!rs.next()) {
                log.fine("[Zstats: "+this.getClass().toString()+"] "+"AFK stat Not found. set it to 0");
                afk_time = 0L;
            }else{
                afk_time = rs.getLong(1);
                log.fine("[Zstats: "+this.getClass().toString()+"] "+"AFK value: " + afk_time);
            }
        }

        if(zstats.get("z:last_played")){
            log.fine("[Zstats: "+this.getClass().toString()+"] "+"Get player last played time from db");
            pss = connection.prepareStatement(ZstatsSqlUtil.getTableName("select val from <$zstats> where uuid=? and stat=?"));
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:last_played");
            rs = pss.executeQuery();
            if (!rs.next()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                log.fine("[Zstats: "+this.getClass().toString()+"] "+"Last played stat Not found. set it to OfflinePlayer#getLastPlayed");
                last_played = p.getLastPlayed()/1000;
            }else{
                last_played = rs.getLong(1);
                log.fine("[Zstats: "+this.getClass().toString()+"] "+"Last played value: " + last_played);
            }
        }

        assert pss != null;
        pss.close();
        rs.close();
        connection.close();
        this.is_updating = false;
    }

    public ZstatsPlayer(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            log.fine("[Zstats: "+this.getClass().toString()+"] "+"Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ZstatsPlayer)) {
            log.fine("[Zstats: "+this.getClass().toString()+"] "+"Not a ZPlayer instance");
            return false;
        }

        // Compare the data members and return accordingly
        boolean result = ((ZstatsPlayer) o).uuid.toString().equals(uuid.toString()) || uuid.toString().equals(((ZstatsPlayer) o).uuid.toString());
        log.fine("[Zstats: "+this.getClass().toString()+"] "+"ZPlayer instance, equal? "+result);
        return result;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    private void clearStat(){
        this.x = new HashMap<>();
        for(Map.Entry<String, Boolean> st: ZstatsConfigs.getVanillaStats().entrySet()){
            if(st.getValue()) x.put(st.getKey(), 0L);
        }
        for(Map.Entry<String, Boolean> st: ZstatsConfigs.getZstats().entrySet()){
            if(st.getValue()) x.put(st.getKey(), 0L);
        }
        this.craft = new LinkedHashMap<>();
        this.place = new LinkedHashMap<>();
        this.mine = new LinkedHashMap<>();
        this.slain = new LinkedHashMap<>();
        this.mob = new LinkedHashMap<>();
    }

    private void rewriteStat(){
        OfflinePlayer p = Bukkit.getOfflinePlayer(this.uuid);
        for(Map.Entry<String, Long> me:x.entrySet()){
            String k = me.getKey();
            if(!k.contains("z:")){
                if(Zstats.getVersion() < 5){
                    if(p.isOnline()) this.x.put(k, (long) Objects.requireNonNull(p.getPlayer()).getStatistic(Statistic.valueOf(k)));
                    else this.x.put(k, (long) players.get(players.indexOf(new ZstatsOldPlayer(this.uuid))).getPlayer().getStatistic(Statistic.valueOf(k)));
                }
                else this.x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
            }else{
                uncommonStat(k);
            }
        }
    }

    private void uncommonStat(String key){ //For world stats or zerrium's custom stat
        switch (key){
            case "z:last_played":
                this.x.put(key, last_played);
                break;

            case "z:afk_time":
                this.x.put(key, afk_time);
                break;

            case "z:world_size":
                this.x.put(key, ZstatsGeneralUtils.world_size);
                break;

            case "z:nether_size":
                this.x.put(key, ZstatsGeneralUtils.nether_size);
                break;

            case "z:end_size":
                this.x.put(key, ZstatsGeneralUtils.end_size);
                break;

            case "z:total_size":
                this.x.put(key, ZstatsGeneralUtils.total_size);
                break;
            default:
                log.fine("[Zstats: "+this.getClass().toString()+"] "+key);
        }
    }

    public void updateStat(Connection connection) throws SQLException { //Should be called Asynchronously
        this.is_updating = true;
        //Clear existing Stats
        this.clearStat();

        //server world save size
        ZstatsGeneralUtils.updateWorldSize();

        //Rewrite with the latest stats
        rewriteStat();

        //substats
        ZstatsSubstats s = new ZstatsSubstats(this);
        s.substats_Material();
        s.substats_Entity();
        s.sort_substats();

        //Update to SQL

        //General stats
        for(Map.Entry<String, Long> me:this.x.entrySet()){
            String k = me.getKey();
            long v = me.getValue();
            if(k.equals("z:world_size") || k.equals("z:nether_size") || k.equals("z:end_size") || k.equals("z:total_size")){
                this.SQL_query(connection, v, "000", k);
            }else{
                this.SQL_query(connection, v, uuid.toString(), k);
            }
        }

        //Crafting stats
        material_looping(connection, this.craft, "z:craft_");

        //Placed items/blocks stats
        material_looping(connection, this.place, "z:place_");

        //Mined blocks stats
        material_looping(connection, this.mine, "z:mine_");

        //Killing stats
        entity_looping(connection, this.mob, "z:mob_");

        //Slain stats
        entity_looping(connection, this.slain, "z:slain_");

        log.info(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Update stats of " + uuid.toString() + " associates with " + name + " done.");
        this.is_updating = false;
        players.remove(new ZstatsOldPlayer(this.uuid));
    }

    private void material_looping(Connection connection, LinkedHashMap<Material, Long> stats, String stat) throws SQLException{
        int j = 1;
        for(Map.Entry<Material, Long> me:stats.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), stat, k.toString(), j);
            j++;
        }
    }

    private void entity_looping(Connection connection, LinkedHashMap<EntityType, Long> stats, String stat) throws SQLException{
        int j = 1;
        for(Map.Entry<EntityType, Long> me:stats.entrySet()){
            EntityType k = me.getKey();
            long v = me.getValue();
            this.SQL_query(connection, v, uuid.toString(), stat, k.toString(), j);
            j++;
        }
    }

    public void deleteStat(Connection connection) throws SQLException { //Should be called Asynchronously
        //delete from SQL
        if(this.is_updating) return;
        log.info(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Deleting stats of " + uuid.toString() + " associates with " + name + " from database...");
        PreparedStatement pss = connection.prepareStatement(ZstatsSqlUtil.getTableName("delete from <$zstats> where uuid=?"));
        pss.setString(1, uuid.toString());
        int row = pss.executeUpdate();
        log.info(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET +
                (row > 0 ? " Deleted stats of " + uuid.toString() + " associates with " + name + " from database." : " No stats of "  + uuid.toString() + " associates with " + name + " found on the database. No rows affected."));
        pss.close();
    }

    private void SQL_query(Connection connection, long val, String uuid, String stat) throws SQLException{ //For general stats
        PreparedStatement pss = connection.prepareStatement(ZstatsSqlUtil.getTableName("select * from <$zstats> where uuid=? and stat=?"));
        pss.setString(1, uuid);
        pss.setString(2, stat);
        ResultSet rs = pss.executeQuery();
        PreparedStatement ps = connection.prepareStatement(ZstatsSqlUtil.getTableName(rs.next() ? "update <$zstats> set val=? where uuid=? and stat=?" : "insert into <$zstats>(val, uuid, stat) values (?, ?, ?)"));
        ps.setLong(1, val);
        ps.setString(2, uuid);
        ps.setString(3, stat);
        log.fine("[Zstats: "+this.getClass().toString()+"] "+ uuid + " - " + stat + " - " + val);
        ps.executeUpdate();
        pss.close();
        rs.close();
        ps.close();
    }

    private void SQL_query(Connection connection, long val, String uuid, String stat, String substat, int j) throws SQLException{ //For substats
        PreparedStatement pss = connection.prepareStatement(ZstatsSqlUtil.getTableName("delete from <$zstats> where uuid=? and stat like ?"));
        pss.setString(1, uuid);
        pss.setString(2, stat + String.format("%04d", j) + "_%");
        pss.executeUpdate();
        PreparedStatement ps = connection.prepareStatement(ZstatsSqlUtil.getTableName("insert into <$zstats>(val, uuid, stat) values (?, ?, ?)"));
        ps.setLong(1, val);
        ps.setString(2, uuid);
        ps.setString(3, stat + String.format("%04d", j) + "_" + substat);
        ps.execute();
        log.fine("[Zstats: "+this.getClass().toString()+"] "+ uuid + " - " + stat + String.format("%04d", j) + "_" + substat + " - " + val);
        pss.close();
        ps.close();
    }
}
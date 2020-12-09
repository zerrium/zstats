package zerrium;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.apache.commons.io.FileUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ZPlayer {
    String name;
    UUID uuid;
    long afk_time, last_played;
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

    public void updateStat(Connection connection) throws SQLException { //Should be called Asynchronously
        x = new HashMap<>();
        craft = new LinkedHashMap<>();
        place = new LinkedHashMap<>();
        mine = new LinkedHashMap<>();
        slain = new LinkedHashMap<>();
        mob = new LinkedHashMap<>();
        this.x.put(Statistic.PLAY_ONE_MINUTE.toString(), 0L);
        this.x.put(Statistic.DAMAGE_DEALT.toString(), 0L);
        this.x.put(Statistic.DAMAGE_TAKEN.toString(), 0L);
        this.x.put(Statistic.MOB_KILLS.toString(), 0L);
        this.x.put(Statistic.DEATHS.toString(), 0L);
        this.x.put(Statistic.SPRINT_ONE_CM.toString(), 0L);
        this.x.put(Statistic.WALK_ONE_CM.toString(), 0L);
        this.x.put(Statistic.CROUCH_ONE_CM.toString(), 0L);
        this.x.put(Statistic.BOAT_ONE_CM.toString(), 0L);
        this.x.put(Statistic.AVIATE_ONE_CM.toString(), 0L);
        this.x.put(Statistic.TRADED_WITH_VILLAGER.toString(), 0L);
        this.x.put(Statistic.TALKED_TO_VILLAGER.toString(), 0L);
        this.x.put(Statistic.CHEST_OPENED.toString(), 0L);
        this.x.put(Statistic.FISH_CAUGHT.toString(), 0L);
        this.x.put(Statistic.ITEM_ENCHANTED.toString(), 0L);
        this.x.put(Statistic.SLEEP_IN_BED.toString(), 0L);
        this.x.put("z:crafted", 0L);
        this.x.put("z:mined", 0L);
        this.x.put("z:pickaxe", 0L);
        this.x.put("z:axe", 0L);
        this.x.put("z:shovel", 0L);
        this.x.put("z:hoe", 0L);
        this.x.put("z:sword", 0L);
        this.x.put("z:bow", 0L);
        this.x.put("z:trident", 0L);
        this.x.put("z:placed", 0L);
        this.x.put("z:craft_kind", 0L);
        this.x.put("z:mine_kind", 0L);
        this.x.put("z:place_kind", 0L);
        this.x.put("z:mob_kind", 0L);
        this.x.put("z:slain_kind", 0L);
        this.x.put("z:last_played", 0L);

        //Not thread safe, cannot do it asynchronously
        OfflinePlayer p = Bukkit.getOfflinePlayer(this.uuid);
        for(Map.Entry<String, Long> me:x.entrySet()){
            String k = me.getKey();
            if(!k.contains("z:")){
                x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
            }else if(k.equals("z:last_played")){
                x.put(k, last_played);
            }
        }

        //server world save size
        Zstats.end_size = 0L;
        Zstats.nether_size = 0L;
        Zstats.world_size = 0L;
        Zstats.total_size = 0L;

        Bukkit.getWorlds().forEach(i ->{
            switch(i.getEnvironment()){
                case NORMAL:
                    Zstats.world_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Zstats.total_size += Zstats.world_size;
                    break;
                case NETHER:
                    Zstats.nether_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Zstats.total_size += Zstats.nether_size;
                    break;
                case THE_END:
                    Zstats.end_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Zstats.total_size += Zstats.end_size;
                    break;
                default:
                    Zstats.total_size += Zstats.total_size;
                    break;
            }
            if(Zstats.debug) System.out.println("Got world size of "+i.getName());
        });
        if(Zstats.debug) System.out.println("Total size "+Zstats.total_size);

        //substat for item crafted, mined and placed
        HashMap<Material, Long> cr = new HashMap<>();
        HashMap<Material, Long> mn = new HashMap<>();
        HashMap<Material, Long> pl = new HashMap<>();

        for(Material m: Material.values()) {
            long a = p.getStatistic(Statistic.CRAFT_ITEM, m);
            long b = p.getStatistic(Statistic.MINE_BLOCK, m);
            long c = p.getStatistic(Statistic.USE_ITEM, m);
            if (a != 0) {
                x.put("z:craft_kind", x.get("z:craft_kind")+1);
                x.put("z:crafted", x.get("z:crafted")+a);
                cr.put(m, a);
            }
            if (b != 0) {
                x.put("z:mine_kind", x.get("z:mine_kind")+1);
                x.put("z:mined", x.get("z:mined")+b);
                mn.put(m, b);
            }
            if (c != 0 && !ZFilter.is_tool(m)) {
                x.put("z:place_kind", x.get("z:place_kind")+1);
                x.put("z:placed", x.get("z:placed")+c);
                pl.put(m, c);
            }else{
                if(m.toString().contains("_PICKAXE")){
                    x.put("z:pickaxe", x.get("z:pickaxe")+c);
                }else if(m.toString().contains("_AXE")){
                    x.put("z:axe", x.get("z:axe")+c);
                }else if(m.toString().contains("_SHOVEL")){
                    x.put("z:shovel", x.get("z:shovel")+c);
                }else if(m.toString().contains("_HOE")){
                    x.put("z:hoe", x.get("z:hoe")+c);
                }else if(m.toString().contains("_SWORD")){
                    x.put("z:sword", x.get("z:sword")+c);
                }else if(m.equals(Material.BOW)){
                    x.put("z:bow", x.get("z:bow")+c);
                }else if(m.equals(Material.TRIDENT)){
                    x.put("z:trident", x.get("z:trident")+c);
                }
            }
        }
        if(Zstats.debug) System.out.println("Materials substat done");

        //substat for kill and killed by
        HashMap<EntityType, Long> ki = new HashMap<>();
        HashMap<EntityType, Long> kb = new HashMap<>();

        for(EntityType t: EntityType.values()){
            try {
                if(t.isAlive()) {
                    long a = p.getStatistic(Statistic.KILL_ENTITY, t);
                    long b = p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                    if (a != 0) {
                        x.put("z:mob_kind", x.get("z:mob_kind") + 1);
                        ki.put(t, a);
                    }
                    if (b != 0) {
                        x.put("z:slain_kind", x.get("z:slain_kind") + 1);
                        kb.put(t, b);
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if(Zstats.debug) System.out.println("EntityType substat done");

        //Sort substats
        if(Zstats.debug) System.out.println("Sorting substats...");
        if(x.get("z:craft_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(cr);
            Iterator x = temp.entrySet().iterator();
            for(int i=0; i<3; i++){
                if(x.hasNext()){
                    Map.Entry e = (Map.Entry) x.next();
                    craft.put((Material) e.getKey(), (Long) e.getValue());
                }else{
                    craft.put(null, 0L);
                }
            }
        }
        if(x.get("z:place_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(pl);
            Iterator x = temp.entrySet().iterator();
            for(int i=0; i<3; i++){
                if(x.hasNext()){
                    Map.Entry e = (Map.Entry) x.next();
                    place.put((Material) e.getKey(), (Long) e.getValue());
                }else{
                    place.put(null, 0L);
                }
            }
        }
        if(x.get("z:mine_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(mn);
            Iterator x = temp.entrySet().iterator();
            for(int i=0; i<3; i++){
                if(x.hasNext()){
                    Map.Entry e = (Map.Entry) x.next();
                    mine.put((Material) e.getKey(), (Long) e.getValue());
                }else{
                    mine.put(null, 0L);
                }
            }
        }
        if(x.get("z:mob_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(ki);
            Iterator x = temp.entrySet().iterator();
            for(int i=0; i<3; i++){
                if(x.hasNext()){
                    Map.Entry e = (Map.Entry) x.next();
                    mob.put((EntityType) e.getKey(), (Long) e.getValue());
                }else{
                    mob.put(null, 0L);
                }
            }
        }
        if(x.get("z:slain_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(kb);
            Iterator x = temp.entrySet().iterator();
            for(int i=0; i<3; i++){
                if(x.hasNext()){
                    Map.Entry e = (Map.Entry) x.next();
                    slain.put((EntityType) e.getKey(), (Long) e.getValue());
                }else{
                    slain.put(null, 0L);
                }
            }
        }
        if(Zstats.debug) System.out.println("Sorting substats done");

        //Update to SQL

        //World Size
        PreparedStatement pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, "000");
        pss.setString(2, "z:world_size");
        ResultSet rs = pss.executeQuery();
        PreparedStatement ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, Zstats.world_size);
        ps.setString(2, "000");
        ps.setString(3, "z:world_size");
        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:world_size" + " - " + Zstats.world_size);
        ps.executeUpdate();

        //Nether Size
        pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, "000");
        pss.setString(2, "z:nether_size");
        rs = pss.executeQuery();
        ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, Zstats.nether_size);
        ps.setString(2, "000");
        ps.setString(3, "z:nether_size");
        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:nether_size" + " - " + Zstats.nether_size);
        ps.executeUpdate();

        //The End Size
        pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, "000");
        pss.setString(2, "z:end_size");
        rs = pss.executeQuery();
        ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, Zstats.end_size);
        ps.setString(2, "000");
        ps.setString(3, "z:end_size");
        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:end_size" + " - " + Zstats.end_size);
        ps.executeUpdate();

        //Total Size
        pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, "000");
        pss.setString(2, "z:total_size");
        rs = pss.executeQuery();
        ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, Zstats.total_size);
        ps.setString(2, "000");
        ps.setString(3, "z:total_size");
        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:total_size" + " - " + Zstats.total_size);
        ps.executeUpdate();

        //AFK time
        pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
        pss.setString(1, uuid.toString());
        pss.setString(2, "z:afk_time");
        rs = pss.executeQuery();
        ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
        ps.setLong(1, afk_time);
        ps.setString(2, uuid.toString());
        ps.setString(3, "z:afk_time");
        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:afk_time" + " - " + afk_time);
        ps.executeUpdate();

        //General stats
        for(Map.Entry<String, Long> me:x.entrySet()){
            String k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("select * from stats where uuid=? and stat=?");
            pss.setString(1, uuid.toString());
            pss.setString(2, k);
            rs = pss.executeQuery();
            ps = connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
            pss.close();
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + k + " - " + v);
            ps.executeUpdate();
            ps.close();
        }

        //Crafting stats
        int j = 1;
        for(Map.Entry<Material, Long> me:craft.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:craft_" + j + "_%");
            pss.executeUpdate();
            ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, "z:craft_" + j + "_" + k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:craft_" + j + "_" + k + " - " + v);
            j++;
        }

        //Placed items/blocks stats
        j = 1;
        for(Map.Entry<Material, Long> me:place.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:place_" + j + "_%");
            pss.executeUpdate();
            ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, "z:place_" + j + "_" + k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:place_" + j + "_" + k + " - " + v);
            ps.executeUpdate();
            j++;
        }

        //Mined blocks stats
        j = 1;
        for(Map.Entry<Material, Long> me:mine.entrySet()){
            Material k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:mine_" + j + "_%");
            pss.executeUpdate();
            ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, "z:mine_" + j + "_" + k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:mine_" + j + "_" + k + " - " + v);
            ps.executeUpdate();
            j++;
        }

        //Killing stats
        j = 1;
        for(Map.Entry<EntityType, Long> me:mob.entrySet()){
            EntityType k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:mob_" + j + "_%");
            pss.executeUpdate();
            ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, "z:mob_" + j + "_" + k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:mob_" + j + "_" + k + " - " + v);
            ps.executeUpdate();
            j++;
        }

        //Slain stats
        j = 1;
        for(Map.Entry<EntityType, Long> me:slain.entrySet()){
            EntityType k = me.getKey();
            long v = me.getValue();

            pss = connection.prepareStatement("delete from stats where uuid=? and stat like ?");
            pss.setString(1, uuid.toString());
            pss.setString(2, "z:slain_" + j + "_%");
            pss.executeUpdate();
            ps = connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
            ps.setLong(1, v);
            ps.setString(2, uuid.toString());
            ps.setString(3, "z:slain_" + j + "_" + k);
            if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:slain_" + j + "_" + k + " - " + v);
            ps.executeUpdate();
            j++;
        }

        pss.close();
        rs.close();
        ps.close();

        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Update stats of " + uuid.toString() + " associates with " + name + " done.");
        if(Zstats.notify_discord && Zstats.has_discordSrv){
            DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")
                    .sendMessage(Zstats.notify_discord_message.replaceAll("<player>".toLowerCase(), name))
                    .queue();
        }

    }

    public void deleteStat(Connection connection) throws SQLException { //Should be called Asynchronously
        //delete from SQL
        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Deleting stats of " + uuid.toString() + " associates with " + name + " from database...");
        PreparedStatement pss = connection.prepareStatement("delete from stats where uuid=?");
        pss.setString(1, uuid.toString());
        int row = pss.executeUpdate();
        System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET +
                (row > 0 ? " Deleted stats of " + uuid.toString() + " associates with " + name + " from database." : " No stats of "  + uuid.toString() + " associates with " + name + " found on the database. No rows affected."));
        pss.close();
    }
}
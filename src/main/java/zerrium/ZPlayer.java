package zerrium;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.apache.commons.io.FileUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ZPlayer {
    String name;
    UUID uuid;
    long afk_time;
    HashMap<String, Long> x; //convert those stupid many attributes into a hashmap
    LinkedHashMap<Material, Long> craft;
    LinkedHashMap<Material, Long> place;
    LinkedHashMap<Material, Long> mine;
    LinkedHashMap<EntityType, Long> slain;
    LinkedHashMap<EntityType, Long> mob;

    public ZPlayer(UUID uuid, String name){
        this.uuid = uuid;
        this.name = name;
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(Zstats.debug) System.out.println("Get player AFK time from db");
                    PreparedStatement pss = Zstats.connection.prepareStatement("select val from stats where uuid=? and stat=?");
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
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
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

    public void updateStat(){
        //Not thread safe, cannot do it asynchronously
        OfflinePlayer p = Bukkit.getOfflinePlayer(this.uuid);
        this.x.forEach((k,v) ->{
            if(!k.contains("z:")){
                assert p != null;
                x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
            }else if(k.equals("z:last_played")){
                assert p != null;
                x.put(k, p.getLastPlayed()/1000);
            }
        });

        //substat for item crafted, mined and placed
        HashMap<Material, Long> cr = new HashMap<>();
        HashMap<Material, Long> mn = new HashMap<>();
        HashMap<Material, Long> pl = new HashMap<>();

        for(Material m: Material.values()) {
            assert p != null;
            long x = p.getStatistic(Statistic.CRAFT_ITEM, m);
            long y = p.getStatistic(Statistic.MINE_BLOCK, m);
            long z = p.getStatistic(Statistic.USE_ITEM, m);
            if (x != 0) {
                this.x.put("z:craft_kind", this.x.get("z:craft_kind")+1);
                this.x.put("z:crafted", this.x.get("z:crafted")+x);
                cr.put(m, x);
            }
            if (y != 0) {
                this.x.put("z:mine_kind", this.x.get("z:mine_kind")+1);
                this.x.put("z:mined", this.x.get("z:mined")+y);
                mn.put(m, y);
            }
            if (z != 0 && !ZFilter.is_tool(m)) {
                this.x.put("z:place_kind", this.x.get("z:place_kind")+1);
                this.x.put("z:placed", this.x.get("z:placed")+z);
                pl.put(m, z);
            }else{
                if(m.toString().contains("_PICKAXE")){
                    this.x.put("z:pickaxe", this.x.get("z:pickaxe")+z);
                }else if(m.toString().contains("_AXE")){
                    this.x.put("z:axe", this.x.get("z:axe")+z);
                }else if(m.toString().contains("_SHOVEL")){
                    this.x.put("z:shovel", this.x.get("z:shovel")+z);
                }else if(m.toString().contains("_HOE")){
                    this.x.put("z:hoe", this.x.get("z:hoe")+z);
                }else if(m.toString().contains("_SWORD")){
                    this.x.put("z:sword", this.x.get("z:sword")+z);
                }else if(m.equals(Material.BOW)){
                    this.x.put("z:bow", this.x.get("z:bow")+z);
                }else if(m.equals(Material.TRIDENT)){
                    this.x.put("z:trident", this.x.get("z:trident")+z);
                }
            }
        }
        if(Zstats.debug) System.out.println("Materials substat done");

        //substat for kill and killed by
        HashMap<EntityType, Long> k = new HashMap<>();
        HashMap<EntityType, Long> kb = new HashMap<>();

        for(EntityType t: EntityType.values()){
            try{
                assert p != null;
                long x = p.getStatistic(Statistic.KILL_ENTITY, t);
                long y = p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                if(x != 0){
                    this.x.put("z:mob_kind", this.x.get("z:mob_kind")+1);
                    k.put(t, x);
                }
                if(y != 0){
                    this.x.put("z:slain_kind", this.x.get("z:slain_kind")+1);
                    kb.put(t, y);
                }
            }catch (IllegalArgumentException e){
                continue;
            }
        }
        if(Zstats.debug) System.out.println("EntityType substat done");

        //server world save size
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
            }
            if(Zstats.debug) System.out.println("Got world size of "+i.getName());
        });
        if(Zstats.debug) System.out.println("Total size "+Zstats.total_size);

        //update to SQL asynchronously
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                    pss.setString(1, "000");
                    pss.setString(2, "z:world_size");
                    ResultSet rs = pss.executeQuery();
                    final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                    pss.close();
                    ps.setLong(1, Zstats.world_size);
                    ps.setString(2, "000");
                    ps.setString(3, "z:world_size");
                    if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:world_size" + " - " + Zstats.world_size);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                try {
                    PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                    pss.setString(1, "000");
                    pss.setString(2, "z:nether_size");
                    ResultSet rs = pss.executeQuery();
                    final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                    pss.close();
                    ps.setLong(1, Zstats.nether_size);
                    ps.setString(2, "000");
                    ps.setString(3, "z:nether_size");
                    if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:nether_size" + " - " + Zstats.nether_size);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                try {
                    PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                    pss.setString(1, "000");
                    pss.setString(2, "z:end_size");
                    ResultSet rs = pss.executeQuery();
                    final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                    pss.close();
                    ps.setLong(1, Zstats.end_size);
                    ps.setString(2, "000");
                    ps.setString(3, "z:end_size");
                    if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:end_size" + " - " + Zstats.end_size);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                try {
                    PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                    pss.setString(1, "000");
                    pss.setString(2, "z:total_size");
                    ResultSet rs = pss.executeQuery();
                    final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                    pss.close();
                    ps.setLong(1, Zstats.total_size);
                    ps.setString(2, "000");
                    ps.setString(3, "z:total_size");
                    if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:total_size" + " - " + Zstats.total_size);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Updating stats of " + uuid.toString() + " associates with " + name + " to database...");
                try {
                    PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                    pss.setString(1, uuid.toString());
                    pss.setString(2, "z:afk_time");
                    ResultSet rs = pss.executeQuery();
                    final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                    pss.close();
                    ps.setLong(1, afk_time);
                    ps.setString(2, uuid.toString());
                    ps.setString(3, "z:afk_time");
                    if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:afk_time" + " - " + afk_time);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                x.forEach((k, v) -> {
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("select * from stats where uuid=? and stat=?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, k);
                        ResultSet rs = pss.executeQuery();
                        final PreparedStatement ps = Zstats.connection.prepareStatement(rs.next() ? "update stats set val=? where uuid=? and stat=?" : "insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                AtomicInteger counter = new AtomicInteger(1);
                craft.forEach((k, v) -> {
                    int j = counter.intValue();
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=? and stat like ?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, "z:craft_" + j + "_%");
                        pss.executeUpdate();
                        final PreparedStatement ps = Zstats.connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, "z:craft_" + j + "_" + k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:craft_" + j + "_" + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                        counter.getAndIncrement();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                counter.set(1);
                place.forEach((k, v) -> {
                    int j = counter.intValue();
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=? and stat like ?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, "z:place_" + j + "_%");
                        pss.executeUpdate();
                        final PreparedStatement ps = Zstats.connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, "z:place_" + j + "_" + k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:place_" + j + "_" + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                        counter.getAndIncrement();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                counter.set(1);
                mine.forEach((k, v) -> {
                    int j = counter.intValue();
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=? and stat like ?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, "z:mine_" + j + "_%");
                        pss.executeUpdate();
                        final PreparedStatement ps = Zstats.connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, "z:mine_" + j + "_" + k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:mine_" + j + "_" + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                        counter.getAndIncrement();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                counter.set(1);
                mob.forEach((k, v) -> {
                    int j = counter.intValue();
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=? and stat like ?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, "z:mob_" + j + "_%");
                        pss.executeUpdate();
                        final PreparedStatement ps = Zstats.connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, "z:mob_" + j + "_" + k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:mob_" + j + "_" + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                        counter.getAndIncrement();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                counter.set(1);
                slain.forEach((k, v) -> {
                    int j = counter.intValue();
                    try {
                        PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=? and stat like ?");
                        pss.setString(1, uuid.toString());
                        pss.setString(2, "z:slain_" + j + "_%");
                        pss.executeUpdate();
                        final PreparedStatement ps = Zstats.connection.prepareStatement("insert into stats(val, uuid, stat) values (?, ?, ?)");
                        pss.close();
                        ps.setLong(1, v);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, "z:slain_" + j + "_" + k);
                        if (Zstats.debug) System.out.println(uuid.toString() + " - " + "z:slain_" + j + "_" + k + " - " + v);
                        ps.executeUpdate();
                        ps.close();
                        counter.getAndIncrement();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

                System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Update stats of " + uuid.toString() + " associates with " + name + " done.");
            }
        };

        //sort substats asynchronously
        BukkitRunnable a = new BukkitRunnable() {
            @Override
            public void run() {
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
                    LinkedHashMap temp = ZFilter.sortByValues(k);
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
                r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
            }
        };
        a.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
    }

    public void deleteStat(){
        //delete from SQL asynchronously
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET + " Deleting stats of " + uuid.toString() + " associates with " + name + " from database...");
                    PreparedStatement pss = Zstats.connection.prepareStatement("delete from stats where uuid=?");
                    pss.setString(1, uuid.toString());
                    int row = pss.executeUpdate();
                    pss.close();
                    System.out.println(ChatColor.YELLOW + "[Zstats]" + ChatColor.RESET +
                            (row > 0 ? " Deleted stats of " + uuid.toString() + " associates with " + name + " from database." : " No stats of "  + uuid.toString() + " associates with " + name + " found on the database. No rows affected."));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(Zstats.getPlugin(Zstats.class));
    }
}
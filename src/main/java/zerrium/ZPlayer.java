package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
    HashMap<String, Long> x; //convert those stupid many attributes into a hashmap
    LinkedHashMap<Material, Long> craft;
    LinkedHashMap<Material, Long> place;
    LinkedHashMap<Material, Long> mine;
    LinkedHashMap<EntityType, Long> slain;
    LinkedHashMap<EntityType, Long> mob;

    public ZPlayer(UUID uuid, String name){
        this.uuid = uuid;
        this.name = name;
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
    }

    public ZPlayer(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            if(SpigotEvent.debug) System.out.println("Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ZPlayer)) {
            if(SpigotEvent.debug) System.out.println("Not a ZPlayer instance");
            return false;
        }

        // Compare the data members and return accordingly
        boolean result = ((ZPlayer) o).uuid.toString().equals(uuid.toString()) || uuid.toString().equals(((ZPlayer) o).uuid.toString());
        if(SpigotEvent.debug) System.out.println("ZPlayer instance, equal? "+result);
        return result;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public void updateStat(){
        //Not thread safe, cannot do it asynchronously
        Player p = Bukkit.getPlayer(this.uuid);
        this.x.forEach((k,v) ->{
            if(!k.contains("z:")){
                if(SpigotEvent.debug) System.out.println(k);
                assert p != null;
                x.put(k, (long) p.getStatistic(Statistic.valueOf(k)));
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
                //this.craft_kind++;
                this.x.put("z:craft_kind", this.x.get("z:craft_kind")+1);
                this.x.put("z:crafted", this.x.get("z:crafted")+x);
                cr.put(m, x);
            }
            if (y != 0) {
                //this.mine_kind++;
                this.x.put("z:mine_kind", this.x.get("z:mine_kind")+1);
                this.x.put("z:mined", this.x.get("z:mined")+y);
                mn.put(m, y);
            }
            if (z != 0 && !ZFilter.is_tool(m)) {
                //this.place_kind++;
                //this.placen += z;
                this.x.put("z:place_kind", this.x.get("z:place_kind")+1);
                this.x.put("z:placed", this.x.get("z:placed")+z);
                pl.put(m, z);
            }
        }
        if(SpigotEvent.debug) System.out.println("Materials substat done");

        //substat for kill and killed by
        HashMap<EntityType, Long> k = new HashMap<>();
        HashMap<EntityType, Long> kb = new HashMap<>();

        for(EntityType t: EntityType.values()){
            if(t.isAlive()){
                assert p != null;
                long x = p.getStatistic(Statistic.KILL_ENTITY, t);
                long y = p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                if(x != 0){
                    //this.mob_kind++;
                    this.x.put("z:mob_kind", this.x.get("z:mob_kind")+1);
                    k.put(t, x);
                }
                if(y != 0){
                    //this.slain_kind++;
                    this.x.put("z:slain_kind", this.x.get("z:slain_kind")+1);
                    k.put(t, y);
                }
            }
        }
        if(SpigotEvent.debug) System.out.println("EntityType substat done");

        //server world save size
        Bukkit.getWorlds().forEach(i ->{
            switch(i.getEnvironment()){
                case NORMAL:
                    Discord.world_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Discord.total_size += Discord.world_size;
                    break;
                case NETHER:
                    Discord.nether_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Discord.total_size += Discord.nether_size;
                    break;
                case THE_END:
                    Discord.end_size = FileUtils.sizeOfDirectory(i.getWorldFolder());
                    Discord.total_size += Discord.end_size;
                    break;
                default:
                    Discord.total_size += FileUtils.sizeOfDirectory(i.getWorldFolder());
            }
            if(SpigotEvent.debug) System.out.println("Got world size of "+i.getName());
        });

        //update to SQL asynchronously
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement pss = SpigotEvent.connection.prepareStatement("select * from stats where uuid=?");
                    pss.setString(1, uuid.toString());
                    ResultSet rs = pss.executeQuery();
                    if(!rs.next()){
                        if(SpigotEvent.debug) System.out.println("Insert stat to MySQL...");
                        final PreparedStatement ps = SpigotEvent.connection.prepareStatement("insert into stats(uuid, stat, val) values" +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?);");
                        AtomicInteger counter = new AtomicInteger(1);
                        x.forEach((k,v) ->{
                            int i = counter.intValue();
                            try {
                                ps.setString(i, uuid.toString());
                                ps.setString(i+1, k);
                                ps.setLong(i+2, v);
                                counter.getAndAdd(3);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        AtomicInteger counter1 = new AtomicInteger(1);
                        craft.forEach((k,v) ->{
                            int i = counter.intValue();
                            int j = counter1.intValue();
                            try {
                                ps.setString(i, uuid.toString());
                                ps.setString(i+1, "z:craft_"+j+"_"+k);
                                ps.setLong(i+2, v);
                                counter.getAndAdd(3);
                                counter1.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter1.set(1);
                        place.forEach((k,v) ->{
                            int i = counter.intValue();
                            int j = counter1.intValue();
                            try{
                                ps.setString(i, uuid.toString());
                                ps.setString(i+2, "z:place_"+j+"_"+k);
                                ps.setLong(i+3, v);
                                counter.getAndAdd(3);
                                counter1.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter1.set(1);
                        mine.forEach((k,v) ->{
                            int i = counter.intValue();
                            int j = counter1.intValue();
                            try{
                                ps.setString(i, uuid.toString());
                                ps.setString(i+2, "z:mine_"+j+"_"+k);
                                ps.setLong(i+3, v);
                                counter.getAndAdd(3);
                                counter1.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter1.set(1);
                        mob.forEach((k,v) ->{
                            int i = counter.intValue();
                            int j = counter1.intValue();
                            try{
                                ps.setString(i, uuid.toString());
                                ps.setString(i+2, "z:mob_"+j+"_"+k);
                                ps.setLong(i+3, v);
                                counter.getAndAdd(3);
                                counter1.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter1.set(1);
                        slain.forEach((k,v) ->{
                            int i = counter.intValue();
                            int j = counter1.intValue();
                            try{
                                ps.setString(i, uuid.toString());
                                ps.setString(i+2, "z:slain_"+j+"_"+k);
                                ps.setLong(i+3, v);
                                counter.getAndAdd(3);
                                counter1.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        ps.executeUpdate();
                        ps.close();
                        if(SpigotEvent.debug) System.out.println("Insert stat to MySQL done");
                    }else{
                        if(SpigotEvent.debug) System.out.println("Update stat to MySQL...");
                        final PreparedStatement ps = SpigotEvent.connection.prepareStatement("update stats set value=? where uuid=? and stat=?");
                        x.forEach((k,v) ->{
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, k);
                                ps.executeUpdate();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        AtomicInteger counter = new AtomicInteger(1);
                        craft.forEach((k,v) ->{
                            int j = counter.intValue();
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, "z:craft_"+j+"_"+k);
                                ps.executeUpdate();
                                counter.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter.set(1);
                        place.forEach((k,v) ->{
                            int j = counter.intValue();
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, "z:place_"+j+"_"+k);
                                ps.executeUpdate();
                                counter.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter.set(1);
                        mine.forEach((k,v) ->{
                            int j = counter.intValue();
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, "z:mine_"+j+"_"+k);
                                ps.executeUpdate();
                                counter.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter.set(1);
                        mob.forEach((k,v) ->{
                            int j = counter.intValue();
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, "z:mob_"+j+"_"+k);
                                ps.executeUpdate();
                                counter.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        counter.set(1);
                        slain.forEach((k,v) ->{
                            int j = counter.intValue();
                            try {
                                ps.setLong(1, v);
                                ps.setString(2, uuid.toString());
                                ps.setString(3, "z:slain_"+j+"_"+k);
                                ps.executeUpdate();
                                counter.getAndIncrement();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });

                        ps.close();
                        if(SpigotEvent.debug) System.out.println("Update stat to MySQL done");
                    }
                    pss.close();
                    rs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        };

        //update to Discord asynchronously
        BukkitRunnable s = new BukkitRunnable() {
            @Override
            public void run() {
                //Javacord API
            }
        };

        //sort substats asynchronously
        BukkitRunnable a = new BukkitRunnable() {
            @Override
            public void run() {
                if(SpigotEvent.debug) System.out.println("Sorting substats...");
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
                if(SpigotEvent.debug) System.out.println("Sorting substats done");
                r.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
                s.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
            }
        };
        a.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
    }
}

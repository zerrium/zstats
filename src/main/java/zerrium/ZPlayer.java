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


public class ZPlayer {
    String name;
    UUID uuid;
    long time_played = 0L, dmg_dealt = 0L, dmg_taken = 0L, mob_kills = 0L, deaths = 0L, sprint = 0L, walk = 0L,
            crouch = 0L, boat = 0L, elytra = 0L, trade = 0L, talk = 0L, chest = 0L, fishing = 0L, enchant = 0L,
            slept = 0L, craft_kind = 0L, place_kind = 0L, mine_kind = 0L, pickaxe = 0L, axe = 0L, shovel = 0L,
            bow = 0L, sword = 0L, trident = 0L, mob_kind = 0L, slain_kind = 0L, craftn = 0L, minen = 0L, placen = 0L, hoe = 0L;
    HashMap<String, Long> x = new HashMap<>(); //convert those stupid many attributes into a hasmap
    LinkedHashMap<Material, Long> craft = new LinkedHashMap<>();
    LinkedHashMap<Material, Long> place = new LinkedHashMap<>();
    LinkedHashMap<Material, Long> mine = new LinkedHashMap<>();
    LinkedHashMap<EntityType, Long> slain = new LinkedHashMap<>();
    LinkedHashMap<EntityType, Long> mob = new LinkedHashMap<>();

    public ZPlayer(UUID uuid, String name){
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public boolean equals (Object v) {
        boolean val = false;

        if (v instanceof ZPlayer){
            ZPlayer x = (ZPlayer) v;
            val = (x.uuid == this.uuid);
        }
        return val;
    }

    public void updateStat(){
        //Not thread safe, cannot do it asynchronously
        Player p = Bukkit.getPlayer(this.uuid);
        this.time_played = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        this.dmg_dealt = p.getStatistic(Statistic.DAMAGE_DEALT);
        this.dmg_taken = p.getStatistic(Statistic.DAMAGE_TAKEN);
        this.mob_kills = p.getStatistic(Statistic.MOB_KILLS);
        this.deaths = p.getStatistic(Statistic.DEATHS);
        this.sprint = p.getStatistic(Statistic.SPRINT_ONE_CM);
        this.walk = p.getStatistic(Statistic.WALK_ONE_CM);
        this.crouch = p.getStatistic(Statistic.CROUCH_ONE_CM);
        this.boat = p.getStatistic(Statistic.BOAT_ONE_CM);
        this.elytra = p.getStatistic(Statistic.AVIATE_ONE_CM);
        this.trade = p.getStatistic(Statistic.TRADED_WITH_VILLAGER);
        this.talk = p.getStatistic(Statistic.TALKED_TO_VILLAGER);
        this.chest = p.getStatistic(Statistic.CHEST_OPENED);
        this.fishing = p.getStatistic(Statistic.FISH_CAUGHT);
        this.enchant = p.getStatistic(Statistic.ITEM_ENCHANTED);
        this.slept = p.getStatistic(Statistic.SLEEP_IN_BED);
        this.craftn = p.getStatistic(Statistic.CRAFT_ITEM);
        this.minen = p.getStatistic(Statistic.MINE_BLOCK);

        this.pickaxe = p.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_PICKAXE);
        this.pickaxe += p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_PICKAXE);
        this.pickaxe += p.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_PICKAXE);
        this.pickaxe += p.getStatistic(Statistic.USE_ITEM, Material.IRON_PICKAXE);
        this.pickaxe += p.getStatistic(Statistic.USE_ITEM, Material.STONE_PICKAXE);
        this.pickaxe += p.getStatistic(Statistic.USE_ITEM, Material.WOODEN_PICKAXE);

        this.axe = p.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_AXE);
        this.axe += p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_AXE);
        this.axe += p.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_AXE);
        this.axe += p.getStatistic(Statistic.USE_ITEM, Material.IRON_AXE);
        this.axe += p.getStatistic(Statistic.USE_ITEM, Material.STONE_AXE);
        this.axe += p.getStatistic(Statistic.USE_ITEM, Material.WOODEN_AXE);

        this.shovel = p.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_SHOVEL);
        this.shovel += p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_SHOVEL);
        this.shovel += p.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_SHOVEL);
        this.shovel += p.getStatistic(Statistic.USE_ITEM, Material.IRON_SHOVEL);
        this.shovel += p.getStatistic(Statistic.USE_ITEM, Material.STONE_SHOVEL);
        this.shovel += p.getStatistic(Statistic.USE_ITEM, Material.WOODEN_SHOVEL);

        this.sword = p.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_SWORD);
        this.sword += p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_SWORD);
        this.sword += p.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_SWORD);
        this.sword += p.getStatistic(Statistic.USE_ITEM, Material.IRON_SWORD);
        this.sword += p.getStatistic(Statistic.USE_ITEM, Material.STONE_SWORD);
        this.sword += p.getStatistic(Statistic.USE_ITEM, Material.WOODEN_SWORD);

        this.hoe = p.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_HOE);
        this.hoe += p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_HOE);
        this.hoe += p.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_HOE);
        this.hoe += p.getStatistic(Statistic.USE_ITEM, Material.IRON_HOE);
        this.hoe += p.getStatistic(Statistic.USE_ITEM, Material.STONE_HOE);
        this.hoe += p.getStatistic(Statistic.USE_ITEM, Material.WOODEN_HOE);

        this.bow = p.getStatistic(Statistic.USE_ITEM, Material.BOW);
        this.trident = p.getStatistic(Statistic.USE_ITEM, Material.TRIDENT);

        //substat for item crafted, mined and placed
        HashMap<Material, Long> cr = new HashMap<>();
        HashMap<Material, Long> mn = new HashMap<>();
        HashMap<Material, Long> pl = new HashMap<>();

        for(Material m: Material.values()) {
            long x = p.getStatistic(Statistic.CRAFT_ITEM, m);
            long y = p.getStatistic(Statistic.MINE_BLOCK, m);
            long z = p.getStatistic(Statistic.USE_ITEM, m);
            if (x != 0) {
                this.craft_kind++;
                cr.put(m, x);
            }
            if (y != 0) {
                this.mine_kind++;
                mn.put(m, y);
            }
            if (z != 0 && !ZFilter.is_tool(m)) {
                this.place_kind++;
                this.placen += z;
                pl.put(m, z);
            }
        }

        //substat for kill and killed by
        HashMap<EntityType, Long> k = new HashMap<>();
        HashMap<EntityType, Long> kb = new HashMap<>();

        for(EntityType t: EntityType.values()){
            if(t.isAlive()){
                long x = p.getStatistic(Statistic.MOB_KILLS, t);
                long y = p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                if(x != 0){
                    this.mob_kind++;
                    k.put(t, x);
                }
                if(y != 0){
                    this.slain_kind++;
                    k.put(t, y);
                }
            }
        }

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
        });

        //update to SQL asynchronously
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement pss = SpigotEvent.connection.prepareStatement("select * from stats where uuid=?");
                    pss.setString(1, uuid.toString());
                    ResultSet rs = pss.executeQuery();
                    pss.close();
                    PreparedStatement ps;
                    if(!rs.next()){
                        ps = SpigotEvent.connection.prepareStatement("insert into stats(uuid, stat, val) values" +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," +
                                "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?)," + "(?, ?, ?);");
                        for(int i=1; i<=138; i+=3){
                            ps.setString(i, uuid.toString());
                        }
                        ps.setString(2, Statistic.PLAY_ONE_MINUTE.toString());
                        ps.setLong(3, time_played);
                        ps.setString(5, Statistic.DAMAGE_DEALT.toString());
                        ps.setLong(6, dmg_dealt);
                        ps.setString(8, Statistic.DAMAGE_TAKEN.toString());
                        ps.setLong(9, dmg_taken);
                        ps.setString(11, Statistic.MOB_KILLS.toString());
                        ps.setLong(12, mob_kills);
                        ps.setString(14, Statistic.DEATHS.toString());
                        ps.setLong(15, deaths);
                        ps.setString(17, Statistic.SPRINT_ONE_CM.toString());
                        ps.setLong(18, sprint);
                        ps.setString(20, Statistic.WALK_ONE_CM.toString());
                        ps.setLong(21, walk);
                        ps.setString(23, Statistic.CROUCH_ONE_CM.toString());
                        ps.setLong(24, crouch);
                        ps.setString(26, Statistic.BOAT_ONE_CM.toString());
                        ps.setLong(27, boat);
                        ps.setString(29, Statistic.AVIATE_ONE_CM.toString());
                        ps.setLong(30, elytra);
                        ps.setString(32, Statistic.TRADED_WITH_VILLAGER.toString());
                        ps.setLong(33, trade);
                        ps.setString(35, Statistic.TALKED_TO_VILLAGER.toString());
                        ps.setLong(36, talk);
                        ps.setString(38, Statistic.CHEST_OPENED.toString());
                        ps.setLong(39, chest);
                        ps.setString(41, Statistic.FISH_CAUGHT.toString());
                        ps.setLong(42, fishing);
                        ps.setString(44, Statistic.ITEM_ENCHANTED.toString());
                        ps.setLong(45, enchant);
                        ps.setString(47, Statistic.SLEEP_IN_BED.toString());
                        ps.setLong(48, slept);
                        ps.setString(50, Statistic.CRAFT_ITEM.toString());
                        ps.setLong(51, craftn);
                        ps.setString(53, Statistic.MINE_BLOCK.toString());
                        ps.setLong(54, minen);
                        ps.setString(56, "pickaxe");
                        ps.setLong(57, pickaxe);
                        ps.setString(59, "axe");
                        ps.setLong(60, axe);
                        ps.setString(62, "shovel");
                        ps.setLong(63, shovel);
                        ps.setString(65, "hoe");
                        ps.setLong(66, hoe);
                        ps.setString(68, "sword");
                        ps.setLong(69, sword);
                        ps.setString(71, "bow");
                        ps.setLong(72, bow);
                        ps.setString(74, "trident");
                        ps.setLong(75, trident);
                        ps.setString(77, "placed");
                        ps.setLong(78, placen);
                        ps.setString(80, "craft_kind");
                        ps.setLong(81, craft_kind);
                        ps.setString(83, "mine_kind");
                        ps.setLong(84, mine_kind);
                        ps.setString(86, "place_kind");
                        ps.setLong(87, place_kind);
                        ps.setString(89, "mob_kind");
                        ps.setLong(90, mob_kind);
                        ps.setString(92, "slain_kind");
                        ps.setLong(93, slain_kind);

                        Object cr[] = craft.keySet().toArray();
                        ps.setString(95, "craft_1_"+cr[0].toString());
                        ps.setLong(96, craft.get((Material) cr[0]));
                        ps.setString(98, "craft_2_"+cr[1].toString());
                        ps.setLong(99, craft.get((Material) cr[1]));
                        ps.setString(101, "craft_3_"+cr[2].toString());
                        ps.setLong(102, craft.get((Material) cr[2]));

                        Object pl[] = place.keySet().toArray();
                        ps.setString(104, "place_1_"+pl[0].toString());
                        ps.setLong(105, place.get((Material) pl[0]));
                        ps.setString(107, "place_2_"+pl[1].toString());
                        ps.setLong(108, place.get((Material) pl[1]));
                        ps.setString(110, "place_3_"+pl[2].toString());
                        ps.setLong(111, place.get((Material) pl[2]));

                        Object mn[] = mine.keySet().toArray();
                        ps.setString(113, "mine_1_"+mn[0].toString());
                        ps.setLong(114, mine.get((Material) mn[0]));
                        ps.setString(116, "mine_2_"+mn[1].toString());
                        ps.setLong(117, mine.get((Material) mn[1]));
                        ps.setString(119, "mine_3_"+mn[2].toString());
                        ps.setLong(120, mine.get((Material) mn[2]));

                        Object mo[] = mob.keySet().toArray();
                        ps.setString(122, "mob_1_"+mo[0].toString());
                        ps.setLong(123, mob.get((EntityType) mo[0]));
                        ps.setString(125, "mob_2_"+mo[1].toString());
                        ps.setLong(126, mob.get((EntityType) mo[1]));
                        ps.setString(128, "mob_3_"+mo[2].toString());
                        ps.setLong(129, mob.get((EntityType) mo[2]));

                        Object sl[] = slain.keySet().toArray();
                        ps.setString(131, "slain_1_"+sl[0].toString());
                        ps.setLong(132, slain.get((EntityType) sl[0]));
                        ps.setString(134, "slain_2_"+sl[1].toString());
                        ps.setLong(135, slain.get((EntityType) sl[1]));
                        ps.setString(137, "slain_3_"+sl[2].toString());
                        ps.setLong(138, slain.get((EntityType) sl[2]));

                        ps.executeUpdate();
                        ps.close();
                    }else{
                        ps = SpigotEvent.connection.prepareStatement("update stats set ");
                    }
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

            }
        };

        //sort substats asynchronously
        BukkitRunnable a = new BukkitRunnable() {
            @Override
            public void run() {
                if(craft_kind != 0){
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
                if(place_kind != 0){
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
                if(mine_kind != 0){
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
                if(mob_kind != 0){
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
                if(slain_kind != 0){
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
                r.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
                s.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
            }
        };
        a.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
    }
}

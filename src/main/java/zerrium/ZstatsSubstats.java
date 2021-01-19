package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.*;

public class ZstatsSubstats { //Manage substats
    final private HashMap<Material, Long> craft;
    final private HashMap<Material, Long> mine;
    final private HashMap<Material, Long> place;
    final private HashMap<EntityType, Long> kill;
    final private HashMap<EntityType, Long> kill_by;
    final private ZstatsPlayer zp;
    final private OfflinePlayer p;

    protected ZstatsSubstats(ZstatsPlayer zp){ //Preparation
        this.craft = new HashMap<>();
        this.mine = new HashMap<>();
        this.place = new HashMap<>();
        this.kill = new HashMap<>();
        this.zp = zp;
        this.kill_by = new HashMap<>();
        this.p = Bukkit.getOfflinePlayer(zp.uuid);
    }

    protected void substats_Material(){ //Substats for crafting, mining and placing blocks or items
        for(Material m: Material.values()) {
            long[] val;

            try{
                val = version_check(m);
            }catch(IllegalArgumentException | NullPointerException | IndexOutOfBoundsException e){
                if(Zstats.debug) System.out.println(m.toString() + ": " + e);
                continue;
            }

            if (val[0] != 0) {
                if(Zstats.zstats.get("z:craft_kind")) zp.x.put("z:craft_kind", zp.x.get("z:craft_kind")+1);
                if(Zstats.zstats.get("z:crafted")){
                    zp.x.put("z:crafted", zp.x.get("z:crafted")+val[0]);
                    this.craft.put(m, val[0]);
                }
            }
            if (val[1] != 0) {
                if(Zstats.zstats.get("z:mine_kind")) zp.x.put("z:mine_kind", zp.x.get("z:mine_kind")+1);
                if(Zstats.zstats.get("z:mined")){
                    zp.x.put("z:mined", zp.x.get("z:mined")+val[1]);
                    this.mine.put(m, val[1]);
                }
            }
            if (val[2] != 0 && !ZstatsFilter.is_tool(m)) {
                if(Zstats.zstats.get("z:place_kind")) zp.x.put("z:place_kind", zp.x.get("z:place_kind")+1);
                if(Zstats.zstats.get("z:placed")){
                    zp.x.put("z:placed", zp.x.get("z:placed")+val[2]);
                    this.place.put(m, val[2]);
                }
            }else{
                substats_Tools(m, val[2]);
            }
        }
        if(Zstats.debug) System.out.println("Materials substat done");
    }

    private void substats_Tools(Material m, long val){
        for(String s : ZstatsFilter.tool_with_material){
            String zstats = ZstatsFilter.tools.get(s);
            if(m.toString().contains(s) && Zstats.zstats.get(zstats)){
                zp.x.put(zstats, zp.x.get(zstats)+val);
                return;
            }
        }
        String zstat = ZstatsFilter.tools.get(m.toString());
        if(Zstats.zstats.get(zstat)) zp.x.put(zstat, zp.x.get(zstat)+val);
    }

    protected void substats_Entity(){ //Substats for killing or killed by entities
        for(EntityType t: EntityType.values()){
            if(t.isAlive()) {
                long[] val;

                try{
                    val = version_check(t);
                }catch(IllegalArgumentException | NullPointerException | IndexOutOfBoundsException e){
                    if(Zstats.debug) System.out.println(t.toString() + ": " + e);
                    continue;
                }

                if (val[0] != 0 && Zstats.zstats.get("z:mob_kind")) {
                    zp.x.put("z:mob_kind", zp.x.get("z:mob_kind") + 1);
                    this.kill.put(t, val[0]);
                }
                if (val[1] != 0 && Zstats.zstats.get("z:slain_kind")) {
                    zp.x.put("z:slain_kind", zp.x.get("z:slain_kind") + 1);
                    this.kill_by.put(t, val[1]);
                }
            }
        }
        if(Zstats.debug) System.out.println("EntityType substat done");
    }

    private long[] version_check(Material m) throws IllegalArgumentException, NullPointerException, IndexOutOfBoundsException{
        long a, b, c;
        if(Zstats.version < 5){
            if(this.p.isOnline()){
                a = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.CRAFT_ITEM, m);
                b = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.MINE_BLOCK, m);
                c = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.USE_ITEM, m);
            }else{
                a = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.CRAFT_ITEM, m);
                b = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.MINE_BLOCK, m);
                c = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.USE_ITEM, m);
            }
        }else{
            a = this.p.getStatistic(Statistic.CRAFT_ITEM, m);
            b = this.p.getStatistic(Statistic.MINE_BLOCK, m);
            c = this.p.getStatistic(Statistic.USE_ITEM, m);
        }
        return new long[]{a, b, c};
    }

    private long[] version_check(EntityType e) throws IllegalArgumentException, NullPointerException, IndexOutOfBoundsException{
        long a, b;
        if(Zstats.version < 5){
            if(p.isOnline()){
                a = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.KILL_ENTITY, e);
                b = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.ENTITY_KILLED_BY, e);
            }else{
                a = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.KILL_ENTITY, e);
                b = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.ENTITY_KILLED_BY, e);
            }
        }else{
            a = this.p.getStatistic(Statistic.KILL_ENTITY, e);
            b = this.p.getStatistic(Statistic.ENTITY_KILLED_BY, e);
        }
        return new long[]{a, b};
    }

    protected void sort_substats(){ //Sort all substats
        if(Zstats.debug) System.out.println("Sorting substats...");
        sort_material("z:craft_kind", this.craft, zp.craft);
        sort_material("z:place_kind", this.place, zp.place);
        sort_material("z:mine_kind", this.mine, zp.mine);
        sort_entity("z:mob_kind", this.kill, zp.mob);
        sort_entity("z:slain_kind", this.kill_by, zp.slain);
        if(Zstats.debug) System.out.println("Sorting substats done");
    }

    private void sort_material(String stat, HashMap<Material, Long> thiss, LinkedHashMap<Material, Long> zpp){
        int i;
        if(zp.x.get(stat) != null && zp.x.get(stat) != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(thiss);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zpp.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
    }

    private void sort_entity(String stat, HashMap<EntityType, Long> thiss, LinkedHashMap<EntityType, Long> zpp){
        int i;
        if(zp.x.get(stat) != null && zp.x.get(stat) != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(thiss);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zpp.put((EntityType) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
    }
}

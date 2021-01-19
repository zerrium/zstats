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
            long a, b, c;

            try{
                if(Zstats.version < 5){
                    if(p.isOnline()){
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
            }catch(IllegalArgumentException | NullPointerException | IndexOutOfBoundsException e){
                if(Zstats.debug) System.out.println(m.toString() + ": " + e);
                continue;
            }

            if (a != 0) {
                if(Zstats.zstats.get("z:craft_kind")) zp.x.put("z:craft_kind", zp.x.get("z:craft_kind")+1);
                if(Zstats.zstats.get("z:crafted")){
                    zp.x.put("z:crafted", zp.x.get("z:crafted")+a);
                    this.craft.put(m, a);
                }
            }
            if (b != 0) {
                if(Zstats.zstats.get("z:mine_kind")) zp.x.put("z:mine_kind", zp.x.get("z:mine_kind")+1);
                if(Zstats.zstats.get("z:mined")){
                    zp.x.put("z:mined", zp.x.get("z:mined")+b);
                    this.mine.put(m, b);
                }
            }
            if (c != 0 && !ZstatsFilter.is_tool(m)) {
                if(Zstats.zstats.get("z:place_kind")) zp.x.put("z:place_kind", zp.x.get("z:place_kind")+1);
                if(Zstats.zstats.get("z:placed")){
                    zp.x.put("z:placed", zp.x.get("z:placed")+c);
                    this.place.put(m, c);
                }
            }else{
                substats_Tools(m, c);
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
                long a, b;

                try{
                    if(Zstats.version < 5){
                        if(p.isOnline()){
                            a = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.KILL_ENTITY, t);
                            b = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.ENTITY_KILLED_BY, t);
                        }else{
                            a = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.KILL_ENTITY, t);
                            b = ZstatsPlayer.players.get(ZstatsPlayer.players.indexOf(new ZstatsOldPlayer(zp.uuid))).getPlayer().getStatistic(Statistic.ENTITY_KILLED_BY, t);
                        }
                    }else{
                        a = this.p.getStatistic(Statistic.KILL_ENTITY, t);
                        b = this.p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                    }
                }catch(IllegalArgumentException | NullPointerException | IndexOutOfBoundsException e){
                    if(Zstats.debug) System.out.println(t.toString() + ": " + e);
                    continue;
                }

                if (a != 0 && Zstats.zstats.get("z:mob_kind")) {
                    zp.x.put("z:mob_kind", zp.x.get("z:mob_kind") + 1);
                    this.kill.put(t, a);
                }
                if (b != 0 && Zstats.zstats.get("z:slain_kind")) {
                    zp.x.put("z:slain_kind", zp.x.get("z:slain_kind") + 1);
                    this.kill_by.put(t, b);
                }
            }
        }
        if(Zstats.debug) System.out.println("EntityType substat done");
    }

    protected void sort_substats(){ //Sort all substats
        if(Zstats.debug) System.out.println("Sorting substats...");
        int i;
        if(zp.x.get("z:craft_kind") != null && zp.x.get("z:craft_kind") != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(this.craft);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.craft.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:place_kind") != null && zp.x.get("z:place_kind") != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(this.place);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.place.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:mine_kind") != null && zp.x.get("z:mine_kind") != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(this.mine);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.mine.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:mob_kind") != null && zp.x.get("z:mob_kind") != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(this.kill);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.mob.put((EntityType) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:slain_kind") != null && zp.x.get("z:slain_kind") != 0){
            LinkedHashMap temp = ZstatsFilter.sortByValues(this.kill_by);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.slain.put((EntityType) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(Zstats.debug) System.out.println("Sorting substats done");
    }
}

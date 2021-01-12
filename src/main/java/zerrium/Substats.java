package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.*;

public class Substats{ //Manage substats
    final private HashMap<Material, Long> craft;
    final private HashMap<Material, Long> mine;
    final private HashMap<Material, Long> place;
    final private HashMap<EntityType, Long> kill;
    final private HashMap<EntityType, Long> kill_by;
    final private ZPlayer zp;
    final private OfflinePlayer p;

    protected Substats(ZPlayer zp){ //Preparation
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

            if(Zstats.version < 5 && p.isOnline()){
                a = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.CRAFT_ITEM, m);
                b = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.MINE_BLOCK, m);
                c = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.USE_ITEM, m);
            }else if(Zstats.version >= 5){
                a = this.p.getStatistic(Statistic.CRAFT_ITEM, m);
                b = this.p.getStatistic(Statistic.MINE_BLOCK, m);
                c = this.p.getStatistic(Statistic.USE_ITEM, m);
            }else{
                return;
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
            if (c != 0 && !ZFilter.is_tool(m)) {
                if(Zstats.zstats.get("z:place_kind")) zp.x.put("z:place_kind", zp.x.get("z:place_kind")+1);
                if(Zstats.zstats.get("z:placed")){
                    zp.x.put("z:placed", zp.x.get("z:placed")+c);
                    this.place.put(m, c);
                }
            }else{
                if(m.toString().contains("_PICKAXE") && Zstats.zstats.get("z:pickaxe")){
                    zp.x.put("z:pickaxe", zp.x.get("z:pickaxe")+c);
                }else if(m.toString().contains("_AXE") && Zstats.zstats.get("z:axe")){
                    zp.x.put("z:axe", zp.x.get("z:axe")+c);
                }else if(m.toString().contains("_SHOVEL") && Zstats.zstats.get("z:shovel")){
                    zp.x.put("z:shovel", zp.x.get("z:shovel")+c);
                }else if(m.toString().contains("_HOE") && Zstats.zstats.get("z:hoe")){
                    zp.x.put("z:hoe", zp.x.get("z:hoe")+c);
                }else if(m.toString().contains("_SWORD") && Zstats.zstats.get("z:sword")){
                    zp.x.put("z:sword", zp.x.get("z:sword")+c);
                }else if(m.equals(Material.BOW) && Zstats.zstats.get("z:bow")){
                    zp.x.put("z:bow", zp.x.get("z:bow")+c);
                }else if(m.equals(Material.SHEARS) && Zstats.zstats.get("z:shears")){
                    zp.x.put("z:shears", zp.x.get("z:shears")+c);
                }else if(m.equals(Material.FLINT_AND_STEEL) && Zstats.zstats.get("z:flint_and_steel")){
                    zp.x.put("z:flint_and_steel", zp.x.get("z:flint_and_steel")+c);
                }else if(Zstats.version>=3 && m.equals(Material.TRIDENT) && Zstats.zstats.get("z:trident")){
                    zp.x.put("z:trident", zp.x.get("z:trident")+c);
                }else if(Zstats.version>=4 && m.equals(Material.CROSSBOW) && Zstats.zstats.get("z:crossbow")){
                    zp.x.put("z:crossbow", zp.x.get("z:crossbow")+c);
                }else if(Zstats.version>=2 && m.equals(Material.SHIELD) && Zstats.zstats.get("z:shield")){
                    zp.x.put("z:shield", zp.x.get("z:shield")+c);
                }
            }
        }
        if(Zstats.debug) System.out.println("Materials substat done");
    }

    protected void substats_Entity(){ //Substats for killing or killed by entities
        for(EntityType t: EntityType.values()){
            try {
                if(t.isAlive()) {
                    long a, b;

                    if(Zstats.version < 5 && p.isOnline()){
                        a = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.KILL_ENTITY, t);
                        b = Objects.requireNonNull(this.p.getPlayer()).getStatistic(Statistic.ENTITY_KILLED_BY, t);
                    }else if(Zstats.version >= 5){
                        a = this.p.getStatistic(Statistic.KILL_ENTITY, t);
                        b = this.p.getStatistic(Statistic.ENTITY_KILLED_BY, t);
                    }else{
                        return;
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
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if(Zstats.debug) System.out.println("EntityType substat done");
    }

    protected void sort_substats(){ //Sort all substats
        if(Zstats.debug) System.out.println("Sorting substats...");
        int i;
        if(zp.x.get("z:craft_kind") != null && zp.x.get("z:craft_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(this.craft);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.craft.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:place_kind") != null && zp.x.get("z:place_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(this.place);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.place.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:mine_kind") != null && zp.x.get("z:mine_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(this.mine);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.mine.put((Material) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:mob_kind") != null && zp.x.get("z:mob_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(this.kill);
            Iterator x = temp.entrySet().iterator();
            i = 0;
            while(x.hasNext() && i<Zstats.substat_top){
                Map.Entry e = (Map.Entry) x.next();
                zp.mob.put((EntityType) e.getKey(), (Long) e.getValue());
                i++;
            }
        }
        if(zp.x.get("z:slain_kind") != null && zp.x.get("z:slain_kind") != 0){
            LinkedHashMap temp = ZFilter.sortByValues(this.kill_by);
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

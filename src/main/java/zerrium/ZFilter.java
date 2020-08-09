package zerrium;

import org.bukkit.Material;

import java.util.*;

public class ZFilter {
    private static final ArrayList<Material> f = new ArrayList<>();
    private static final ArrayList<String> g = new ArrayList<>();

    public ZFilter(){
        f.add(Material.BOW);
        f.add(Material.TRIDENT);
        f.add(Material.SHEARS);
        f.add(Material.FLINT_AND_STEEL);
        f.add(Material.SHIELD);
        f.add(Material.COMPASS);

        g.add("_PICKAXE");
        g.add("_AXE");
        g.add("_SHOVEL");
        g.add("_SWORD");
        g.add("_HOE");
    }

    public static boolean is_tool(Material m){
        if (f.contains(m)) return true;
        else{
            for(String s: g){
                if(m.toString().contains(s)) return true;
            }
        }
        return false;
    }

    public static LinkedHashMap sortByValues(HashMap map) {
        List<Object> list = new LinkedList<Object>(map.entrySet());
        // Defined Custom Comparator here
        list.sort(Collections.reverseOrder((o1, o2) -> ((Comparable<Object>) ((Map.Entry) (o1)).getValue())
                .compareTo(((Map.Entry) (o2)).getValue())));

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        LinkedHashMap sortedHashMap = new LinkedHashMap();
        for (Object o : list) {
            Map.Entry entry = (Map.Entry) o;
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}

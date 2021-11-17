package zerrium.utils;

import org.bukkit.Material;
import zerrium.Zstats;

import java.util.*;

public class ZstatsFilter {
    public static final ArrayList<Material> tool = new ArrayList<>();
    public static final ArrayList<String> tool_with_material = new ArrayList<>();
    public static final LinkedHashMap<String, String> tools = new LinkedHashMap<>();

    public static void begin(){
        final int version = Zstats.getVersion();

        tool.addAll(List.of(
                Material.BOW,
                Material.SHEARS,
                Material.FLINT_AND_STEEL,
                Material.COMPASS
        ));
        if(Zstats.getVersion() >=4) tool.add(Material.CROSSBOW); //1.14+
        if(Zstats.getVersion() >=3) tool.add(Material.TRIDENT); //1.13+
        if(Zstats.getVersion() >=2) tool.add(Material.SHIELD); //1.9+

        tool_with_material.addAll(List.of(
                "_PICKAXE",
                "_AXE",
                "_SHOVEL",
                "_SWORD",
                "_HOE"
        ));

        tools.putAll(Map.ofEntries(
                Map.entry("_PICKAXE", "z:pickaxe"),
                Map.entry("_AXE", "z:axe"),
                Map.entry("_SHOVEL", "z:shovel"),
                Map.entry("_HOE", "z:hoe"),
                Map.entry("_SWORD", "z:sword"),
                Map.entry(Material.BOW.toString(), "z:bow"),
                Map.entry(Material.SHEARS.toString(), "z:shears"),
                Map.entry(Material.FLINT_AND_STEEL.toString(), "z:flint_and_steel")
        ));
        if(version >= 3) tools.put(Material.TRIDENT.toString(), "z:trident"); //1.13+
        if(version >= 4) tools.put(Material.CROSSBOW.toString(), "z:crossbow"); //1.14+
        if(version >= 2) tools.put(Material.SHIELD.toString(), "z:shield"); //1.9+
    }

    public static boolean is_tool(Material m){
        if (tool.contains(m)) return true;
        else{
            for(String s: tool_with_material){
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

package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;

public class ZstatsFilter {
    private static final ArrayList<Material> tool = new ArrayList<>();
    protected static final ArrayList<String> tool_with_material = new ArrayList<>();
    protected static final LinkedHashMap<String, String> tools = new LinkedHashMap<>();

    protected static void begin(){
        tool.add(Material.BOW);
        if(Zstats.version >=4) tool.add(Material.CROSSBOW); //1.14+
        if(Zstats.version >=3) tool.add(Material.TRIDENT); //1.13+
        tool.add(Material.SHEARS);
        tool.add(Material.FLINT_AND_STEEL);
        if(Zstats.version >=2) tool.add(Material.SHIELD); //1.9+
        tool.add(Material.COMPASS);

        tool_with_material.add("_PICKAXE");
        tool_with_material.add("_AXE");
        tool_with_material.add("_SHOVEL");
        tool_with_material.add("_SWORD");
        tool_with_material.add("_HOE");

        tools.put("_PICKAXE", "z:pickaxe");
        tools.put("_AXE", "z:axe");
        tools.put("_SHOVEL", "z:shovel");
        tools.put("_HOE", "z:hoe");
        tools.put("_SWORD", "z:sword");
        tools.put(Material.BOW.toString(), "z:bow");
        tools.put(Material.SHEARS.toString(), "z:shears");
        tools.put(Material.FLINT_AND_STEEL.toString(), "z:flint_and_steel");
        if(Zstats.version >= 3) tools.put(Material.TRIDENT.toString(), "z:trident"); //1.13+
        if(Zstats.version >= 4) tools.put(Material.CROSSBOW.toString(), "z:crossbow"); //1.14+
        if(Zstats.version >= 2) tools.put(Material.SHIELD.toString(), "z:shield"); //1.9+
    }

    protected static boolean is_tool(Material m){
        if (tool.contains(m)) return true;
        else{
            for(String s: tool_with_material){
                if(m.toString().contains(s)) return true;
            }
        }
        return false;
    }

    protected static LinkedHashMap sortByValues(HashMap map) {
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

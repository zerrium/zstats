package zerrium;

import org.bukkit.Material;
import org.bukkit.Statistic;

import java.util.ArrayList;

public class ZFilter {
    private static ArrayList<Material> f = new ArrayList<>();

    public ZFilter(){
        f.add(Material.NETHERITE_PICKAXE);
        f.add(Material.DIAMOND_PICKAXE);
        f.add(Material.GOLDEN_PICKAXE);
        f.add(Material.IRON_PICKAXE);
        f.add(Material.STONE_PICKAXE);
        f.add(Material.WOODEN_PICKAXE);
        f.add(Material.NETHERITE_AXE);
        f.add(Material.DIAMOND_AXE);
        f.add(Material.GOLDEN_AXE);
        f.add(Material.IRON_AXE);
        f.add(Material.STONE_AXE);
        f.add(Material.WOODEN_AXE);
        f.add(Material.NETHERITE_SHOVEL);
        f.add(Material.DIAMOND_SHOVEL);
        f.add(Material.GOLDEN_SHOVEL);
        f.add(Material.IRON_SHOVEL);
        f.add(Material.STONE_SHOVEL);
        f.add(Material.WOODEN_SHOVEL);
        f.add(Material.NETHERITE_SWORD);
        f.add(Material.DIAMOND_SWORD);
        f.add(Material.GOLDEN_SWORD);
        f.add(Material.IRON_SWORD);
        f.add(Material.STONE_SWORD);
        f.add(Material.WOODEN_SWORD);
        f.add(Material.NETHERITE_HOE);
        f.add(Material.DIAMOND_HOE);
        f.add(Material.GOLDEN_HOE);
        f.add(Material.IRON_HOE);
        f.add(Material.STONE_HOE);
        f.add(Material.WOODEN_HOE);
        f.add(Material.BOW);
        f.add(Material.TRIDENT);
        f.add(Material.SHEARS);
        f.add(Material.FLINT_AND_STEEL);
        f.add(Material.SHIELD);
    }

    public static boolean is_tool(Material m){
        return f.contains(m);
    }
}

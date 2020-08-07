package zerrium;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;


public class ZPlayer {
    String name;
    UUID uuid;
    long time_played = 0L, dmg_dealt = 0L, dmg_taken = 0L, mob_kills = 0L, deaths = 0L, sprint = 0L, walk = 0L,
            crouch = 0L, boat = 0L, elytra = 0L, trade = 0L, talk = 0L, chest = 0L, fishing = 0L, enchant = 0L,
            slept = 0L, craft_kind = 0L, place_kind = 0L, mine_kind = 0L, pickaxe = 0L, axe = 0L, shovel = 0L,
            bow = 0L, sword = 0L, trident = 0L, mob_kind = 0L, slain_kind = 0L, craftn = 0L, minen = 0L, placen = 0L, hoe = 0L;
    Top3 craft = new Top3("", 0L, "", 0L, "", 0L),
            place = new Top3("", 0L, "", 0L, "", 0L),
            mine = new Top3("", 0L, "", 0L, "", 0L),
            slain = new Top3("", 0L, "", 0L, "", 0L),
            mob = new Top3("", 0L, "", 0L, "", 0L);
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

        for(Material m: Material.values()) {
            if (p.getStatistic(Statistic.CRAFT_ITEM, m) != 0) {
                this.craft_kind++;
            }
            if (p.getStatistic(Statistic.MINE_BLOCK, m) != 0) {
                this.mine_kind++;
            }
            if (p.getStatistic(Statistic.USE_ITEM, m) != 0 && !ZFilter.is_tool(m)) {
                this.place_kind++;
                this.placen += p.getStatistic(Statistic.USE_ITEM, m);
            }
        }

        //update to SQL asynchronously
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {

            }
        };

        //update to Discord asynchronously
        BukkitRunnable s = new BukkitRunnable() {
            @Override
            public void run() {

            }
        };

        r.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
        s.runTaskAsynchronously(SpigotEvent.getPlugin(SpigotEvent.class));
    }
}

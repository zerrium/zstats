package zerrium.models;

import java.util.LinkedList;
import java.util.List;

public enum ZstatsConfig {
    DB_HOST         ("hostname"),
    DB_PORT         ("port"),
    DB_NAME         ("database"),
    DB_USER         ("username"),
    DB_PASSWORD     ("password"),
    DB_TABLE        ("table_name_prefix"),
    DB_SSL          ("use_SSL"),
    NOTIFY_DISCORD  ("notify_stats_update_to_discord"),
    DISCORD_MESSAGE ("notify_message"),
    SUBSTAT_TOP     ("zstats_top"),
    VANILLA_STATS   ("vanilla_stats"),
    ZSTATS          ("zstats"),
    DEBUG           ("use_debug");

    private final String config;

    ZstatsConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return this.config;
    }

    public static LinkedList<ZstatsConfig> getBooleanConfigs() {
        return new LinkedList<>(List.of(
                DB_SSL,
                NOTIFY_DISCORD,
                DEBUG
        ));
    }

    public static LinkedList<ZstatsConfig> getIntConfigs() {
        return new LinkedList<>(List.of(
                DB_PORT,
                SUBSTAT_TOP
        ));
    }

    public static LinkedList<ZstatsConfig> getStringConfigs() {
        return new LinkedList<>(List.of(
                DB_HOST,
                DB_NAME,
                DB_USER,
                DB_PASSWORD,
                DB_TABLE,
                DISCORD_MESSAGE
        ));
    }
}

package com.ericlam.mc.leadersystem.config;

import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.ericlam.mc.leadersystem.sign.SignData;
import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeaderConfig extends ConfigSetter {
    public static FileConfiguration signData;
    public static String rankNull, notValue, playerNull, createSignSuccess, signRemoved, forceUpdated, getStatistic, getStatisticPlayer, noStatistic, notInLimit;
    public static Set<LeaderBoard> leaderBoards = new HashSet<>();
    private String prefix;
    public static int guiRow, selectLimit;
    private static File signDataFile;
    private static Plugin plugin;
    private ConfigManager configManager;
    public static final Map<Sign, SignData> signDataMap = new ConcurrentHashMap<>();

    public LeaderConfig(Plugin plugin) {
        super(plugin, "leaders.yml", "lang.yml", "signs.yml", "config.yml");
        signDataFile = new File(plugin.getDataFolder(), "signs.yml");
        LeaderConfig.plugin = plugin;
        prefix = HyperNiteMC.getAPI().getCoreConfig().getPrefix();
        configManager = HyperNiteMC.getAPI().registerConfig(this);
        configManager.setMsgConfig("lang.yml");
    }

    private static boolean saveSign() {
        try {
            signData.save(signDataFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void reloadConfig() {
        configManager.reloadAllConfigs();
        this.loadMessages();
    }

    private String translate(String path) {
        return configManager.getMessage(path);
    }

    public static void saveSignData() {
        if (signData == null || signDataFile == null) return;
        int i = 0;
        while (!saveSign()) {
            if (i > 5) break;
            plugin.getLogger().info("Sign Data saving failed, try " + (5 - i) + " more times...");
            i++;
        }
    }

    private String[] translate(List<String> list) {
        return list.stream().map(line -> ChatColor.translateAlternateColorCodes('&', prefix + line)).toArray(String[]::new);
    }

    private List<String> translateList(List<String> list) {
        return list.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
    }

    private String translate(String path, FileConfiguration config) {
        return ChatColor.translateAlternateColorCodes('&', Optional.ofNullable(config.getString(path)).orElse(""));
    }


    @Override
    public void loadConfig(Map<String, FileConfiguration> map) {
        signData = map.get("signs.yml");
        signDataMap.clear();
        for (String key : signData.getKeys(false)) {
            String item = signData.getString(key.concat(".item"));
            int rank = signData.getInt(key.concat(".rank"));
            String w = signData.getString(key.concat(".world"));
            World world = w == null ? null : Bukkit.getWorld(w);
            if (world == null) continue;
            Vector v = Optional.ofNullable(signData.getConfigurationSection(key.concat(".head-location"))).map(s -> Vector.deserialize(s.getValues(false))).orElse(null);
            BlockVector vector = v == null ? null : v.toBlockVector();
            Vector signV = Optional.ofNullable(signData.getConfigurationSection(key.concat(".sign-location"))).map(s -> Vector.deserialize(s.getValues(false))).orElse(null);
            if (signV == null) continue;
            Location loc = signV.toLocation(world);
            if (!(loc.getBlock().getState() instanceof WallSign)) return;
            Sign sign = (Sign) loc.getBlock().getState(false);
            signDataMap.put(sign, new SignData(item, key, rank, world, vector));
        }
        leaderBoards.clear();
        FileConfiguration leader = map.get("leaders.yml");
        for (String key : leader.getKeys(false)) {
            String database = leader.getString(key + ".database");
            String table = leader.getString(key + ".table");
            String column = leader.getString(key + ".column");
            String name = leader.getString(key + ".player-name");
            String uuid = leader.getString(key + ".player-uuid");
            String show = leader.getString(key + ".data-show");
            String invTitle = translate(key + ".inv-title", leader);
            String invName = translate(key + ".inv-name", leader);
            List<String> signs = translateList(leader.getStringList(key + ".sign")).subList(0, 4);
            List<String> lores = translateList(leader.getStringList(key + ".inv-lores"));
            leaderBoards.add(new LeaderBoard(key, database, table, column, name, uuid, show, signs, lores, invTitle, invName));
        }


        FileConfiguration config = map.get("config.yml");
        guiRow = config.getInt("gui-row");
        selectLimit = config.getInt("select-limit");
    }

    public void loadMessages() {
        rankNull = translate("rank-null");
        notValue = translate("not-value");
        playerNull = translate("player-null");
        createSignSuccess = translate("sign-create-success");
        signRemoved = translate("sign-removed");
        forceUpdated = translate("force-updated");
        getStatistic = translate("get-statistic");
        getStatisticPlayer = translate("get-statistic-player");
        noStatistic = translate("no-statistic");
        notInLimit = translate("not-in-limit");
    }
}

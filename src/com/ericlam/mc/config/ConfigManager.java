package com.ericlam.mc.config;

import com.ericlam.mc.model.LeaderBoard;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager {
    public static FileConfiguration signData;
    public static String rankNull, notValue, playerNull, createSignSuccess, signRemoved, forceUpdated, getStatistic, getStatisticPlayer, noStatistic, notInLimit;
    public static String[] help;
    public static Set<LeaderBoard> leaderBoards = new HashSet<>();
    private String prefix;
    public static int guiSize, selectLimit;
    private static File signDataFile;
    private static Plugin plugin;
    private FileConfiguration leader, msg, config;
    public ConfigManager(Plugin plugin){
        ConfigManager.plugin = plugin;
        File leaderFile = new File(plugin.getDataFolder(), "leaders.yml");
        File msgFile = new File(plugin.getDataFolder(),"lang.yml");
        signDataFile = new File(plugin.getDataFolder(), "signs.yml");
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!leaderFile.exists()) plugin.saveResource("leaders.yml",true);
        if (!msgFile.exists()) plugin.saveResource("lang.yml",true);
        if (!signDataFile.exists()) plugin.saveResource("signs.yml", true);
        if (!configFile.exists()) plugin.saveResource("config.yml", true);
        msg = YamlConfiguration.loadConfiguration(msgFile);
        leader = YamlConfiguration.loadConfiguration(leaderFile);
        signData = YamlConfiguration.loadConfiguration(signDataFile);
        config = YamlConfiguration.loadConfiguration(configFile);
        prefix = com.hypernite.config.ConfigManager.getInstance().getPrefix();
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

    private String translate(String path) {
        return ChatColor.translateAlternateColorCodes('&', prefix + msg.getString(path));
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

    public void loadConfig(){
        for (String key : leader.getKeys(false)) {
            String database = leader.getString(key+".database");
            String table = leader.getString(key+".table");
            String column = leader.getString(key+".column");
            String name = leader.getString(key+".player-name");
            String uuid = leader.getString(key+".player-uuid");
            String show = leader.getString(key+".data-show");
            String invTitle = translate(key + ".inv-title", leader);
            String invName = translate(key + ".inv-name", leader);
            List<String> signs = translateList(leader.getStringList(key+".sign")).subList(0,4);
            List<String> lores = translateList(leader.getStringList(key+".inv-lores"));
            leaderBoards.add(new LeaderBoard(key, database, table, column, name, uuid, show, signs, lores, invTitle, invName));
        }

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
        help = translate(msg.getStringList("help"));

        guiSize = config.getInt("gui-size");
        selectLimit = config.getInt("select-limit");

    }

    private String translate(String path, FileConfiguration config) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }



}

package com.ericlam.mc.config;

import com.ericlam.mc.model.LeaderBoard;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager {
    private FileConfiguration leader;
    private FileConfiguration msg;
    public static Set<LeaderBoard> leaderBoards = new HashSet<>();
    private String prefix;
    public static String rankNull,notValue,playerNull,createSignSuccess;
    public ConfigManager(Plugin plugin){
        File leaderFile = new File(plugin.getDataFolder(), "leaders.yml");
        File msgFile = new File(plugin.getDataFolder(),"lang.yml");
        if (!leaderFile.exists()) plugin.saveResource("leaders.yml",true);
        if (!msgFile.exists()) plugin.saveResource("lang.yml",true);
        msg = YamlConfiguration.loadConfiguration(msgFile);
        leader = YamlConfiguration.loadConfiguration(leaderFile);
        prefix = com.hypernite.config.ConfigManager.getInstance().getPrefix();
    }

    public void loadConfig(){
        for (String key : leader.getKeys(false)) {
            String database = leader.getString(key+".database");
            String table = leader.getString(key+".table");
            String column = leader.getString(key+".column");
            String name = leader.getString(key+".player-name");
            String uuid = leader.getString(key+".player-uuid");
            String show = leader.getString(key+".data-show");
            int limit = leader.getInt(key+".limit");
            List<String> signs = translateList(leader.getStringList(key+".sign")).subList(0,4);
            List<String> lores = translateList(leader.getStringList(key+".inv-lores"));
            leaderBoards.add(new LeaderBoard(key,database,table,column,name,uuid,show,limit,signs,lores));
        }

        rankNull = translate("rank-null");
        notValue = translate("not-value");
        playerNull = translate("player-null");
        createSignSuccess = translate("sign-create-success");

    }

    private String translate(String path){
        return ChatColor.translateAlternateColorCodes('&',prefix+msg.getString(path));
    }

    private String[] translate(List<String> list){
        return list.stream().map(line -> ChatColor.translateAlternateColorCodes('&',prefix+line)).toArray(String[]::new);
    }

    private List<String> translateList(List<String> list){
        return list.stream().map(line -> ChatColor.translateAlternateColorCodes('&',line)).collect(Collectors.toList());
    }



}

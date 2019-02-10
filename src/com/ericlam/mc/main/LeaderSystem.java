package com.ericlam.mc.main;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.listener.onSignCreated;
import com.ericlam.mc.manager.LeaderBoardManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderSystem extends JavaPlugin {
    public static Plugin plugin;
    @Override
    public void onEnable() {
        plugin = this;
        new ConfigManager(this).loadConfig();
        this.getLogger().info("LeaderSystem Enabled.");
        this.getServer().getPluginManager().registerEvents(new onSignCreated(this),this);
        LeaderBoardManager leaderBoardManager = LeaderBoardManager.getInstance();
        leaderBoardManager.startUpdateScheduler();
    }
}

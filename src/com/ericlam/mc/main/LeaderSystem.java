package com.ericlam.mc.main;

import com.ericlam.mc.commandhandler.LeaderSystemCommand;
import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.listener.onSignEvent;
import com.ericlam.mc.manager.LeaderBoardManager;
import com.ericlam.mc.manager.LeaderInventoryManager;
import com.ericlam.mc.placeholders.PlaceholderHook;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderSystem extends JavaPlugin {
    @Override
    public void onEnable() {
        new ConfigManager(this).loadConfig();
        this.getLogger().info("LeaderSystem Enabled.");
        this.getServer().getPluginManager().registerEvents(new onSignEvent(this), this);
        this.getCommand("leadersystem").setExecutor(new LeaderSystemCommand(this));
        LeaderBoardManager leaderBoardManager = LeaderBoardManager.getInstance();
        LeaderInventoryManager leaderInventoryManager = LeaderInventoryManager.getInstance();
        leaderBoardManager.startUpdateScheduler(this);
        leaderBoardManager.startSignUpdate(this);
        leaderInventoryManager.inventoryUpdateScheduler(this);
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Found PlaceholderAPI! registering placeholders...");
            new PlaceholderHook(this).register();
        }
    }
}

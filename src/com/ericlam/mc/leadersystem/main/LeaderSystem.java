package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.commandhandler.LeaderSystemCommand;
import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.listener.onSignEvent;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.placeholders.PlaceholderHook;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderSystem extends JavaPlugin {

    private static LeaderConfig leaderConfig;

    public static LeaderConfig getLeaderConfig() {
        return leaderConfig;
    }

    @Override
    public void onEnable() {
        leaderConfig = new LeaderConfig(this);
        leaderConfig.loadMessages();
        this.getLogger().info("LeaderSystem Enabled.");
        this.getServer().getPluginManager().registerEvents(new onSignEvent(this), this);
        new LeaderSystemCommand(this).register();
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

package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.commandhandler.LeaderSystemCommand;
import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.listener.onSignEvent;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.placeholders.PlaceholderHook;
import com.ericlam.mc.leadersystem.runnables.DataUpdateRunnable;
import com.hypernite.mc.hnmc.core.utils.Tools;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderSystem extends JavaPlugin {

    private static LeaderConfig leaderConfig;
    private static LeaderBoardManager leaderBoardManager;
    private static LeaderInventoryManager leaderInventoryManager;
    private Runnable clearCache;

    public static LeaderConfig getLeaderConfig() {
        return leaderConfig;
    }

    public static LeaderBoardManager getLeaderBoardManager() {
        return leaderBoardManager;
    }

    public static LeaderInventoryManager getLeaderInventoryManager() {
        return leaderInventoryManager;
    }

    public Runnable getClearCache() {
        return clearCache;
    }

    @Override
    public void onEnable() {
        leaderConfig = new LeaderConfig(this);
        leaderConfig.loadMessages();
        this.getLogger().info("LeaderSystem Enabled.");
        this.getServer().getPluginManager().registerEvents(new onSignEvent(this), this);
        leaderBoardManager = new LeaderBoardManager();
        leaderInventoryManager = new LeaderInventoryManager(leaderBoardManager, this);
        clearCache = () -> {
            leaderBoardManager.clearCache();
            leaderInventoryManager.clearCache();
        };
        new DataUpdateRunnable(this).runTaskTimerAsynchronously(this, Tools.randomWithRange(1200, 3601) * 20L, 3600 * 20L);
        new LeaderSystemCommand(this).register();
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Found PlaceholderAPI! registering placeholders...");
            new PlaceholderHook(this).register();
        }
    }
}

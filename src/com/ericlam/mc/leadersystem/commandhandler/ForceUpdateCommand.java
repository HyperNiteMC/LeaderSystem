package com.ericlam.mc.leadersystem.commandhandler;

import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ForceUpdateCommand extends BukkitRunnable {
    private final Plugin plugin;
    private LeaderBoardManager leaderBoardManager;
    private LeaderInventoryManager leaderInventoryManager;

    ForceUpdateCommand(Plugin plugin) {
        this.plugin = plugin;
        leaderBoardManager = LeaderBoardManager.getInstance();
        leaderInventoryManager = LeaderInventoryManager.getInstance();
    }

    @Override
    public void run() {
        leaderInventoryManager.forceUpdateInv();
        leaderBoardManager.forceUpdateSigns(plugin);
        leaderBoardManager.forceUpdateSQL();
    }
}

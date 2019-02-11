package com.ericlam.mc.commandhandler;

import com.ericlam.mc.manager.LeaderBoardManager;
import com.ericlam.mc.manager.LeaderInventoryManager;
import org.bukkit.Bukkit;
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            leaderInventoryManager.forceUpdateInv();
            leaderBoardManager.forceUpdateSigns(plugin);
            leaderBoardManager.forceUpdateSQL();
        });
    }
}

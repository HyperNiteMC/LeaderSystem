package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.manager.CacheManager;
import com.ericlam.mc.leadersystem.manager.LoadManager;
import com.ericlam.mc.leadersystem.manager.SignManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncUpdateRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final ConcurrentLinkedQueue<String> items;
    private final LoadManager loadManager;
    private final CacheManager cacheManager;
    private final SignManager signManager;

    public AsyncUpdateRunnable(LeaderSystem system, String item) {
        this(system, of(item));
    }

    public AsyncUpdateRunnable(LeaderSystem system, ConcurrentLinkedQueue<String> items) {
        this.items = items;
        this.plugin = system;
        this.loadManager = system.getLoadManager();
        this.cacheManager = system.getCacheManager();
        this.signManager = system.getSignManager();
    }

    private static ConcurrentLinkedQueue<String> of(String item) {
        var queue = new ConcurrentLinkedQueue<String>();
        queue.offer(item);
        return queue;
    }

    @Override
    public void run() {
        var item = items.poll();
        if (item != null) {
            try {
                var boards = loadManager.getRankingFromSQL(item);
                cacheManager.setLeaderBoard(item, boards);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    var inventory = loadManager.loadLeaderInventory(item, boards);
                    cacheManager.setLeaderInventory(item, inventory);
                    signManager.updateSigns(item);
                    plugin.getLogger().info("Updated leaderboard " + item);
                });
            } catch (LeaderBoardNonExistException e) {
                plugin.getLogger().warning("Cannot find item " + e.getItem() + ", skipped update");
            }
        }
    }
}

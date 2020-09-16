package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ScheduleUpdateRunnable extends BukkitRunnable {

    private final LeaderSystem leaderSystem;
    private final LeadersConfig leadersConfig;
    private final ConcurrentLinkedQueue<String> itemQueue = new ConcurrentLinkedQueue<>();

    public ScheduleUpdateRunnable(LeaderSystem leaderSystem) {
        this.leaderSystem = leaderSystem;
        this.leadersConfig = leaderSystem.getYamlManager().getConfigAs(LeadersConfig.class);
    }

    @Override
    public void run() {
        leaderSystem.getLogger().info("Updating leaderboard data...");
        leadersConfig.stats.keySet().forEach(key -> {
            itemQueue.offer(key);
            new AsyncUpdateRunnable(leaderSystem, itemQueue).runTaskAsynchronously(leaderSystem);
        });
    }
}

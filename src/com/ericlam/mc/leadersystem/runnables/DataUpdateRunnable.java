package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.main.LeaderSystem;
import org.bukkit.scheduler.BukkitRunnable;

public class DataUpdateRunnable extends BukkitRunnable {

    private LeaderSystem leaderSystem;

    public DataUpdateRunnable(LeaderSystem leaderSystem) {
        this.leaderSystem = leaderSystem;
    }

    @Override
    public void run() {
        leaderSystem.getClearCache().run();
        new SignUpdateRunnable(LeaderSystem.getLeaderBoardManager()).runTaskLater(leaderSystem, 300L);

    }
}

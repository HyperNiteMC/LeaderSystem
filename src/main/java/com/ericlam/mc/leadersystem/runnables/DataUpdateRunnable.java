package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.main.LeaderSystem;
import org.bukkit.scheduler.BukkitRunnable;

public class DataUpdateRunnable extends BukkitRunnable {

    private final LeaderSystem leaderSystem;

    public DataUpdateRunnable(LeaderSystem leaderSystem) {
        this.leaderSystem = leaderSystem;
    }

    @Override
    public void run() {
        leaderSystem.getClearCache().run();
        if (!SignUpdateRunnable.running)
            new SignUpdateRunnable(LeaderSystem.getLeaderBoardManager()).runTaskLater(leaderSystem, 300L);

    }
}

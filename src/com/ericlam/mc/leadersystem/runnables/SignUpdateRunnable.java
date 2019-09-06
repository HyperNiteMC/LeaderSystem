package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SignUpdateRunnable extends BukkitRunnable {

    private LeaderBoardManager boardManager;

    static boolean running = false;

    public SignUpdateRunnable(LeaderBoardManager boardManager) {
        this.boardManager = boardManager;
        running = true;
    }

    @Override
    public void run() {
        if (LeaderConfig.signDataMap.isEmpty()) return;
        if (!boardManager.getCaching().isEmpty()) {
            boardManager.updateSignData();
            running = false;
        } else {
            new SignUpdateRunnable(boardManager).runTaskLater(LeaderSystem.getProvidingPlugin(LeaderSystem.class), 300L);
        }
    }
}

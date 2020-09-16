package com.ericlam.mc.leadersystem.runnables;

import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SignUpdateRunnable extends BukkitRunnable {

    static boolean running = false;
    private final LeaderBoardManager boardManager;

    public SignUpdateRunnable(LeaderBoardManager boardManager) {
        this.boardManager = boardManager;
        running = true;
    }

    @Override
    public void run() {
        if (LeaderSystem.getYamlManager().getConfigAs(SignConfig.class).signs.isEmpty()) return;
        if (!boardManager.getCaching().isEmpty()) {
            boardManager.updateSignData();
            running = false;
        } else {
            new SignUpdateRunnable(boardManager).runTaskLater(LeaderSystem.getProvidingPlugin(LeaderSystem.class), 300L);
        }
    }
}

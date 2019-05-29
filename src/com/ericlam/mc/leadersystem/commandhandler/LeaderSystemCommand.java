package com.ericlam.mc.leadersystem.commandhandler;

import com.ericlam.mc.leadersystem.config.ConfigManager;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.model.Board;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.misc.commands.*;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.TreeSet;

public class LeaderSystemCommand {
    private LeaderSystem leaderSystem;
    private DefaultCommand root;


    public LeaderSystemCommand(LeaderSystem leaderSystem) {
        this.leaderSystem = leaderSystem;
        CommandNode help = new CommandNodeBuilder("help").description("指令幫助").alias("?").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    commandSender.sendMessage(ConfigManager.help);
                    return true;
                }).build();

        CommandNode update = new CommandNodeBuilder("update").description("強制更新排行戰績").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    new ForceUpdateCommand(leaderSystem).runTaskAsynchronously(leaderSystem);
                    commandSender.sendMessage(ConfigManager.forceUpdated);
                    return true;
                }).build();

        CommandNode get = new CommandNodeBuilder("get").description("獲得自己/別人戰績的排行與數值").permission(Perm.ADMIN)
                .placeholder("<stats> [player]")
                .execute((commandSender, list) -> {
                    if (!(commandSender instanceof Player)) {
                        commandSender.sendMessage("not player");
                        return false;
                    }
                    Player player = (Player) commandSender;
                    if (list.size() < 2) {
                        Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                            this.runAsync(() -> {
                                TreeSet<Board> boardsList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
                                Utils.getBoard(boardsList, player.getUniqueId()).ifPresentOrElse(board ->
                                                player.sendMessage(ConfigManager.getStatistic.replaceAll("<item>", leaderBoard.getItem())
                                                        .replaceAll("<rank>", board.getRank() + "")
                                                        .replaceAll("<data>", board.getDataShow())),
                                        () -> player.sendMessage(ConfigManager.notInLimit.replace("<limit>", ConfigManager.selectLimit + "")));
                            });
                        }, () -> player.sendMessage(ConfigManager.noStatistic));
                    } else {
                        String target = list.get(1);
                        Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                            this.runAsync(() -> {
                                TreeSet<Board> boardsList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
                                Utils.getBoard(boardsList, target).ifPresentOrElse(board ->
                                                player.sendMessage(ConfigManager.getStatisticPlayer.replaceAll("<player>", target)
                                                        .replaceAll("<item>", leaderBoard.getItem())
                                                        .replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow())),
                                        () -> player.sendMessage(ConfigManager.notInLimit.replace("<limit>", ConfigManager.selectLimit + "")));
                            });
                        }, () -> player.sendMessage(ConfigManager.noStatistic));
                    }
                    return true;
                }).build();

        CommandNode inv = new CommandNodeBuilder("inv").description("打開該戰績的排行界面").alias("openinv", "gui").placeholder("<stats>")
                .execute((commandSender, list) -> {
                    if (!(commandSender instanceof Player)) {
                        commandSender.sendMessage("not player");
                        return false;
                    }
                    Player player = (Player) commandSender;
                    Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                        this.runAsync(() -> {
                            Inventory inventory = LeaderInventoryManager.getInstance().getLeaderInventory(leaderBoard);
                            Bukkit.getScheduler().runTask(leaderSystem, () -> player.openInventory(inventory));
                        });
                    }, () -> player.sendMessage(ConfigManager.noStatistic));
                    return true;
                }).build();

        CommandNode test = new AdvCommandNodeBuilder<Player>("test").description("Test command").execute((sender, list) -> {
            sender.setAllowFlight(!sender.getAllowFlight());
            sender.sendMessage("your fly is now: " + sender.getAllowFlight());
            return true;
        }).build();

        this.root = new DefaultCommandBuilder("leadersystem").description("LeaderSystem 主指令").children(help, update, get, inv, test).build();
    }


    private void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(leaderSystem, runnable);
    }

    public void register() {
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(leaderSystem, this.root);
    }
}

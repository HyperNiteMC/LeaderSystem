package com.ericlam.mc.leadersystem.commandhandler;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.model.Board;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.misc.commands.*;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.TreeSet;

public class LeaderSystemCommand {
    private LeaderSystem leaderSystem;
    private DefaultCommand root;


    public LeaderSystemCommand(LeaderSystem leaderSystem) {
        this.leaderSystem = leaderSystem;
        CommandNode update = new CommandNodeBuilder("update").description("強制更新排行戰績").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    new ForceUpdateCommand(leaderSystem).runTaskAsynchronously(leaderSystem);
                    commandSender.sendMessage(LeaderConfig.forceUpdated);
                    return true;
                }).build();

        CommandNode reload = new CommandNodeBuilder("reload").description("重載 yml").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    LeaderSystem.getLeaderConfig().reloadConfig();
                    commandSender.sendMessage(ChatColor.GREEN + "重載成功。");
                    return true;
                }).build();

        CommandNode get = new CommandNodeBuilder("get").description("獲得自己/別人戰績的排行與數值").permission(Perm.ADMIN)
                .placeholder("<stats> [player]")
                .execute((commandSender, list) -> {
                    if (list.size() < 2) {
                        if (!(commandSender instanceof Player)) {
                            commandSender.sendMessage("not player");
                            return false;
                        }
                        Player player = (Player) commandSender;
                        Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                            this.runAsync(() -> {
                                TreeSet<Board> boardsList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
                                Utils.getBoard(boardsList, player.getUniqueId()).ifPresentOrElse(board ->
                                                player.sendMessage(LeaderConfig.getStatistic.replaceAll("<item>", leaderBoard.getItem())
                                                        .replaceAll("<rank>", board.getRank() + "")
                                                        .replaceAll("<data>", board.getDataShow())),
                                        () -> player.sendMessage(LeaderConfig.notInLimit.replace("<limit>", LeaderConfig.selectLimit + "")));
                            });
                        }, () -> player.sendMessage(LeaderConfig.noStatistic));
                    } else {
                        String target = list.get(1);
                        Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                            this.runAsync(() -> {
                                TreeSet<Board> boardsList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
                                Utils.getBoard(boardsList, target).ifPresentOrElse(board ->
                                                commandSender.sendMessage(LeaderConfig.getStatisticPlayer.replaceAll("<player>", target)
                                                        .replaceAll("<item>", leaderBoard.getItem())
                                                        .replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow())),
                                        () -> commandSender.sendMessage(LeaderConfig.notInLimit.replace("<limit>", LeaderConfig.selectLimit + "")));
                            });
                        }, () -> commandSender.sendMessage(LeaderConfig.noStatistic));
                    }
                    return true;
                }).build();

        CommandNode inv = new AdvCommandNodeBuilder<Player>("inv").description("打開該戰績的排行界面").alias("openinv", "gui").placeholder("<stats>")
                .execute((player, list) -> {
                    Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                        this.runAsync(() -> {
                            Inventory inventory = LeaderInventoryManager.getInstance().getLeaderInventory(leaderBoard);
                            Bukkit.getScheduler().runTask(leaderSystem, () -> player.openInventory(inventory));
                        });
                    }, () -> player.sendMessage(LeaderConfig.noStatistic));
                    return true;
                }).build();

        this.root = new DefaultCommandBuilder("leadersystem").description("LeaderSystem 主指令").children(update, get, inv, reload).build();
    }


    private void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(leaderSystem, runnable);
    }

    public void register() {
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(leaderSystem, this.root);
    }
}

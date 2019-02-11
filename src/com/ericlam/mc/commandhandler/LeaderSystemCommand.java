package com.ericlam.mc.commandhandler;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.main.LeaderSystem;
import com.ericlam.mc.main.Utils;
import com.ericlam.mc.manager.LeaderBoardManager;
import com.ericlam.mc.model.Board;
import com.ericlam.mc.model.LeaderBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class LeaderSystemCommand implements CommandExecutor {
    private final Plugin plugin;
    private String noPerm;

    public LeaderSystemCommand(LeaderSystem plugin) {
        this.plugin = plugin;
        com.hypernite.config.ConfigManager cf = com.hypernite.config.ConfigManager.getInstance();
        noPerm = cf.getPrefix() + cf.getNoPerm();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage("§eLeaderSystem §a" + plugin.getDescription().getVersion() + "§e made by §b" + plugin.getDescription().getAuthors());
            return true;
        }

        if (!commandSender.hasPermission("hypernite.admin")) {
            commandSender.sendMessage(noPerm);
            return false;
        }

        if (strings.length == 1) {
            switch (strings[0]) {
                case "help":
                    commandSender.sendMessage(ConfigManager.help);
                    return true;
                case "update":
                    new ForceUpdateCommand(plugin).run();
                    commandSender.sendMessage(ConfigManager.forceUpdated);
                    return true;
                default:
                    commandSender.sendMessage(ConfigManager.help);
                    return true;
            }
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Not player!");
            return false;
        }

        Player player = (Player) commandSender;

        if (!strings[0].equals("get")) {
            player.sendMessage(ConfigManager.help);
            return false;
        }

        String item = strings[1];
        LeaderBoard leaderBoard = Utils.getItem(item);

        if (leaderBoard == null) {
            player.sendMessage(ConfigManager.noStatistic);
            return false;
        }

        if (strings.length == 2) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<Board> boardList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
                Board board = Utils.getBoard(boardList, player.getUniqueId());
                if (board == null) {
                    player.sendMessage(ConfigManager.notInLimit.replace("<limit>", ConfigManager.selectLimit + ""));
                    return;
                }
                player.sendMessage(ConfigManager.getStatistic.replaceAll("<item>", leaderBoard.getItem()).replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow()));
            });
            return true;
        }

        String target = strings[2];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Board> boardList = LeaderBoardManager.getInstance().getRanking(leaderBoard);
            Board board = Utils.getBoard(boardList, target);
            if (board == null) {
                player.sendMessage(ConfigManager.notInLimit.replace("<limit>", ConfigManager.selectLimit + ""));
                return;
            }
            player.sendMessage(ConfigManager.getStatisticPlayer.replaceAll("<player>", target).replaceAll("<item>", leaderBoard.getItem()).replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow()));
        });

        return true;
    }
}

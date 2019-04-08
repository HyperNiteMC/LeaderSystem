package com.ericlam.mc.placeholders;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.main.LeaderSystem;
import com.ericlam.mc.main.Utils;
import com.ericlam.mc.manager.LeaderBoardManager;
import com.ericlam.mc.model.Board;
import com.ericlam.mc.model.LeaderBoard;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.TreeSet;

public class PlaceholderHook extends PlaceholderExpansion {

    private final Plugin plugin;
    private final LeaderBoardManager leaderBoardManager;

    private final String NO_THIS_STATISTIC = "無此戰績";
    private final String NOT_ENOUGH_ARGS = "格式錯誤";
    private final String NOT_IN_LIMIT;

    public PlaceholderHook(LeaderSystem plugin) {
        this.plugin = plugin;
        leaderBoardManager = LeaderBoardManager.getInstance();
        NOT_IN_LIMIT = "不在前" + ConfigManager.selectLimit + "名之內";
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if (p == null) return "";
        String[] args = params.split("_");
        if (args.length != 2) return NOT_ENOUGH_ARGS;
        String item = args[1];
        LeaderBoard leaderBoard = Utils.getItem(item);
        if (leaderBoard == null) return NO_THIS_STATISTIC;
        TreeSet<Board> boardList = leaderBoardManager.getRanking(leaderBoard);
        switch (args[0]) {
            case "rank":
                Board board = Utils.getBoard(boardList, p.getUniqueId());
                if (board == null) return NOT_IN_LIMIT;
                return board.getRank() + "";
            case "first":
                Board boardFirst = Utils.getBoard(boardList, 1);
                if (boardFirst == null || boardFirst.getPlayerName() == null || boardFirst.getPlayerUUID() == null)
                    return null;
                return boardFirst.getPlayerName();
            default:
                break;
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "leadersystem";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}

package com.ericlam.mc.main;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.model.LeaderBoard;
import com.ericlam.mc.model.Board;

import java.util.List;

public class Utils {

    public static LeaderBoard getItem(String item){
        for (LeaderBoard leaderBoard : ConfigManager.leaderBoards) {
            if (leaderBoard.getItem().equalsIgnoreCase(item)) return leaderBoard;
        }
        return null;
    }

    public static Board getBoardFromRank(List<Board> boards, int rank){
        for (Board board : boards) {
            if (board.getRank() == rank) return board;
        }
        return null;
    }
}

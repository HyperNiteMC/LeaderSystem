package com.ericlam.mc.main;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.model.Board;
import com.ericlam.mc.model.LeaderBoard;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.UUID;

public class Utils {

    public static LeaderBoard getItem(String item){
        for (LeaderBoard leaderBoard : ConfigManager.leaderBoards) {
            if (leaderBoard.getItem().equalsIgnoreCase(item)) return leaderBoard;
        }
        return null;
    }

    public static Board getBoard(List<Board> boards, int rank) {
        for (Board board : boards) {
            if (board.getRank() == rank) return board;
        }
        return null;
    }

    public static Board getBoard(List<Board> boards, UUID uuid) {
        for (Board board : boards) {
            if (board.getPlayerUUID() == null || board.getPlayerName() == null) continue;
            if (board.getPlayerUUID().toString().equals(uuid.toString())) return board;
        }
        return null;
    }

    public static Board getBoard(List<Board> boards, String name) {
        for (Board board : boards) {
            if (board.getPlayerUUID() == null || board.getPlayerName() == null) continue;
            if (board.getPlayerName().equals(name)) return board;
        }
        return null;
    }

    public static String uidGenerator() {
        String uid;
        do {
            uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        } while (ConfigManager.signData.contains(uid));
        return uid;
    }

    public static String getUidFromLoc(Location location) {
        FileConfiguration signData = ConfigManager.signData;
        for (String key : signData.getKeys(false)) {
            Location loc = (Location) signData.get(key + ".location");
            if (loc == null) continue;
            if (loc.equals(location)) return key;
        }
        return null;
    }


}

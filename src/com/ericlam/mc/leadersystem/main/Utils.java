package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

public class Utils {

    public static Optional<LeaderBoard> getItem(String item) {
        return LeaderConfig.leaderBoards.stream().filter(leaderBoard -> leaderBoard.getItem().equals(item)).findAny();
    }

    public static Optional<Board> getBoard(TreeSet<Board> boards, int rank) {
        return boards.stream().filter(board -> board.getRank() == rank).findAny();
    }

    public static Optional<Board> getBoard(TreeSet<Board> boards, UUID uuid) {
        return boards.stream().filter(board -> {
            if (board.getPlayerUUID() != null && board.getPlayerName() != null) {
                return board.getPlayerUUID().toString().equals(uuid.toString());
            }
            return false;
        }).findAny();
    }

    public static Optional<Board> getBoard(TreeSet<Board> boards, String name) {
        return boards.stream().filter(board -> {
            if (board.getPlayerUUID() != null && board.getPlayerName() != null) {
                return board.getPlayerName().equals(name);
            }
            return false;
        }).findAny();
    }

    public static String uidGenerator() {
        String uid;
        do {
            uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        } while (LeaderConfig.signData.contains(uid));
        return uid;
    }

    @Nullable
    public static String getUidFromLoc(Location location) {
        FileConfiguration signData = LeaderConfig.signData;
        for (String key : signData.getKeys(false)) {
            Location loc = getLocationFromConfig(signData, key);
            if (loc != null && loc.equals(location)) return key;
        }
        return null;
    }

    public static Location getLocationFromConfig(FileConfiguration signData, String key) {
        World world = Bukkit.getWorld(signData.getString(key + ".world"));
        if (world == null) return null;
        double x = signData.getDouble(key + ".location.x");
        double y = signData.getDouble(key + ".location.y");
        double z = signData.getDouble(key + ".location.z");
        return new Location(world, x, y, z);
    }

    public static Location getLocationFromConfig(FileConfiguration signData, String key, String locKey) {
        World world = Bukkit.getWorld(signData.getString(key + ".world"));
        if (world == null) return null;
        double x = signData.getDouble(key + "." + locKey + ".x");
        double y = signData.getDouble(key + "." + locKey + ".y");
        double z = signData.getDouble(key + "." + locKey + ".z");
        return new Location(world, x, y, z);
    }


}

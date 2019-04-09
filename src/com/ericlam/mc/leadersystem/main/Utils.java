package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.config.ConfigManager;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.TreeSet;
import java.util.UUID;

public class Utils {

    public static LeaderBoard getItem(String item){
        for (LeaderBoard leaderBoard : ConfigManager.leaderBoards) {
            if (leaderBoard.getItem().equalsIgnoreCase(item)) return leaderBoard;
        }
        return null;
    }

    public static Board getBoard(TreeSet<Board> boards, int rank) {
        for (Board board : boards) {
            if (board.getRank() == rank) return board;
        }
        return null;
    }

    public static Board getBoard(TreeSet<Board> boards, UUID uuid) {
        for (Board board : boards) {
            if (board.getPlayerUUID() == null || board.getPlayerName() == null) continue;
            if (board.getPlayerUUID().toString().equals(uuid.toString())) return board;
        }
        return null;
    }

    public static Board getBoard(TreeSet<Board> boards, String name) {
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

package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.ericlam.mc.leadersystem.sign.SignData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    private static String uidGenerator() {
        String uid;
        do {
            uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        } while (LeaderConfig.signData.contains(uid));
        return uid;
    }

    @Nullable
    public static SignData getSignData(Sign sign) {
        return LeaderConfig.signDataMap.get(sign);
    }

    public static CompletableFuture<Void> removeSign(@Nonnull SignData signData) {
        return CompletableFuture.runAsync(() -> {
            LeaderConfig.signData.set(signData.getUid(), null);
            LeaderConfig.saveSignData();
        });
    }

    public static void assignData(@Nonnull Sign signState, @Nonnull TreeSet<Board> boards, @Nonnull LeaderBoard leaderBoard) {
        SignData data = Utils.getSignData(signState);
        if (data == null) {
            Bukkit.getLogger().warning("[LeaderSystem] sign data is null, skipped");
            return;
        }
        Optional<Board> boardOptional = Utils.getBoard(boards, data.getRank());
        if (boardOptional.isEmpty()) {
            Bukkit.getLogger().warning("[LeaderSystem] board is empty , skipped.");
            return;
        }
        Board board = boardOptional.get();
        if (board.getPlayerUUID() == null) {
            Bukkit.getLogger().warning("[LeaderSystem] sign data is null, skipped.");
            return;
        }
        signState.setEditable(true);
        final String playerName = board.getPlayerName().equalsIgnoreCase("null") ? ChatColor.RED + "[! 找不到名稱]" : board.getPlayerName();
        for (int i = 0; i < 4; i++) {
            String line = leaderBoard.getSigns().get(i)
                    .replaceAll("<rank>", board.getRank() + "")
                    .replaceAll("<player>", playerName)
                    .replaceAll("<data>", board.getDataShow());
            signState.setLine(i, line);
        }
        signState.update(true);
        Bukkit.getLogger().info("[LeaderSystem] sign data for ".concat(playerName).concat(" is updated."));
    }

    public static void saveSignData(Block sign, Board board, LeaderBoard leaderBoard, Vector headBlock, String uid) {
        Location signLoc = sign.getLocation();
        FileConfiguration signData = LeaderConfig.signData;
        signData.set(uid + ".item", leaderBoard.getItem());
        signData.set(uid + ".rank", board.getRank());
        signData.set(uid + ".world", signLoc.getWorld().getName());
        signData.createSection(uid + ".location", signLoc.toVector().serialize());
        signData.createSection(uid + ".head-location", headBlock.serialize());
        LeaderConfig.saveSignData();
    }


}

package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.model.Board;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

public class Utils {

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

    public static boolean isWalled(BlockFace face, Block head) {
        return head.getRelative(face).getType() != Material.AIR;
    }

    public static String vectorToUID(Vector vector) {
        return vector.toString().replaceAll("\\.0", "");
    }


}

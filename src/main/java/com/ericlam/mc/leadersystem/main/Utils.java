package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.model.Board;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Utils {


    public static Optional<LeadersConfig.LeaderBoard> getItem(String item) {
        return Optional.ofNullable(LeaderSystem.getYamlManager().getConfigAs(LeadersConfig.class).stats.get(item));
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

    @Nullable
    public static Sign getState(SignConfig.SignData data) {
        var vector = data.signLocation.toBlockVector();
        World world = Bukkit.getWorld(data.world);
        if (world == null) return null;
        Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        if (!(block.getState() instanceof Sign)) return null;
        return (Sign) block.getState(false);
    }

    @Nullable
    public static SignConfig.SignData getSignData(BlockVector vector) {
        return LeaderSystem.getYamlManager().getConfigAs(SignConfig.class).signs.values().stream().filter(data -> data.signLocation.toBlockVector().equals(vector)).findAny().orElse(null);
    }


    public static CompletableFuture<Void> removeSign(@Nonnull SignConfig.SignData signData) {
        return CompletableFuture.runAsync(() -> {
            var sign = LeaderSystem.getYamlManager().getConfigAs(SignConfig.class);
            sign.signs.values().removeIf(s -> s.equals(signData));
            try {
                sign.save();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    public static boolean isWalled(BlockFace face, Block head) {
        return head.getRelative(face).getType() != Material.AIR;
    }

    public static String vectorToUID(Vector vector) {
        return vector.toString().replaceAll("\\.0", "");
    }

    public static void assignData(@Nonnull Sign signState, @Nonnull TreeSet<Board> boards, @Nonnull LeadersConfig.LeaderBoard leaderBoard, @Nullable BlockFace newFace) {
        SignConfig.SignData data = Utils.getSignData(signState.getLocation().toVector().toBlockVector());
        if (data == null) {
            Bukkit.getLogger().warning("[LeaderSystem] sign data is null, skipped");
            return;
        }
        Optional<Board> boardOptional = Utils.getBoard(boards, data.rank);
        if (boardOptional.isEmpty()) {
            Bukkit.getLogger().warning("[LeaderSystem] board is empty , skipped.");
            return;
        }
        Board board = boardOptional.get();
        if (board.getPlayerUUID() == null) {
            Bukkit.getLogger().warning("[LeaderSystem] sign data is null, skipped.");
            return;
        }
        BlockVector headVector = data.headLocation.toBlockVector();
        World world = Bukkit.getWorld(data.world);
        if (world == null) return;
        if (headVector != null) {
            Block head = headVector.toLocation(world).getBlock();
            BlockFace face;
            BlockData blockData = head.getBlockData();
            if (blockData instanceof Directional) {
                face = ((Directional) blockData).getFacing();
            } else if (blockData instanceof Rotatable) {
                face = ((Rotatable) blockData).getRotation();
            } else if (newFace != null) {
                face = newFace;
            } else {
                Bukkit.getLogger().warning("[LeaderSystem] " + headVector.toString() + " is not a player head");
                return;
            }
            boolean walled = isWalled(face.getOppositeFace(), head);
            if (board.getPlayerName().equalsIgnoreCase("null")) {
                HyperNiteMC.getAPI().getPlayerSkinManager().setHeadBlock(board.getPlayerUUID(), head, walled, face);
            } else {
                HyperNiteMC.getAPI().getPlayerSkinManager().setHeadBlock(board.getPlayerUUID(), board.getPlayerName(), head, walled, face);
            }
        }

        signState.setEditable(true);
        final String playerName = board.getPlayerName().equalsIgnoreCase("null") ? ChatColor.RED + "[! 找不到名稱]" : board.getPlayerName();
        for (int i = 0; i < 4; i++) {
            String line = leaderBoard.sign.get(i)
                    .replaceAll("<rank>", board.getRank() + "")
                    .replaceAll("<player>", playerName)
                    .replaceAll("<data>", board.getDataShow());
            signState.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
        }
        signState.update(true);
        Bukkit.getLogger().info("[LeaderSystem] sign data for ".concat(playerName).concat(" is updated."));
    }


}

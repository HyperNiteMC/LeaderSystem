package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.config.SignVector;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class SignManager {

    private final SignConfig signConfig;
    private final LeadersConfig leadersConfig;
    private final CacheManager cacheManager;
    private final Plugin plugin;

    public SignManager(LeaderSystem system) {
        this.plugin = system;
        var yamlManager = system.getYamlManager();
        this.cacheManager = system.getCacheManager();
        this.signConfig = yamlManager.getConfigAs(SignConfig.class);
        this.leadersConfig = yamlManager.getConfigAs(LeadersConfig.class);
    }


    public Optional<LeaderSign> parseSign(String[] lines) throws NumberFormatException {
        if (lines.length < 2) return Optional.empty();
        String item = lines[0];
        String rankStr = Optional.ofNullable(lines[1]).orElse("");
        LeadersConfig.LeaderBoard leaderBoard = leadersConfig.stats.get(item);
        if (leaderBoard == null) return Optional.empty();
        int rank = Integer.parseInt(rankStr);
        return Optional.of(new LeaderSign(item, rank));
    }

    public void createSign(Block sign, Player player, LeaderSign leaderSign) {
        Vector headVector = sign.getLocation().add(0, 1, 0).toVector();
        Block headBlock = headVector.toLocation(sign.getWorld()).getBlock();
        boolean walled = Utils.isWalled(player.getFacing(), headBlock);
        if (!walled) {
            Block signRelative = sign.getRelative(player.getFacing());
            headVector = signRelative.getLocation().add(0, 1, 0).toVector();
        }
        final String uid = Utils.vectorToUID(sign.getLocation().toVector());
        SignConfig.SignData data = new SignConfig.SignData();
        data.signLocation = SignVector.parse(sign.getLocation().toVector().toBlockVector());
        data.headLocation = SignVector.parse(headVector.toBlockVector());
        data.world = sign.getWorld().getName();
        data.item = leaderSign.item;
        data.rank = leaderSign.rank;
        signConfig.signs.put(uid, data);
        try {
            signConfig.save();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    @Nullable
    private Sign getState(SignConfig.SignData data) {
        var vector = data.signLocation.toBlockVector();
        World world = Bukkit.getWorld(data.world);
        if (world == null) return null;
        Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        if (!(block.getState() instanceof Sign)) return null;
        return (Sign) block.getState(false);
    }

    public void updateSigns() {
        signConfig.signs.values().stream().map(this::getState).filter(Objects::nonNull).forEach(this::updateSign);
    }

    public void updateSigns(String item) {
        signConfig.signs.values().stream().filter(s -> s.item.equals(item)).map(this::getState).filter(Objects::nonNull).forEach(this::updateSign);
    }

    // for update
    public void updateSign(Sign signState) {
        this.updateSign(signState, null);
    }

    @Nonnull
    public String getUidFromSign(Sign sign) {
        return Utils.vectorToUID(sign.getLocation().toVector());
    }

    public SignConfig.SignData getSignData(Sign sign) {
        return signConfig.signs.get(getUidFromSign(sign));
    }

    // for creation
    public void updateSign(Sign signState, BlockFace newFace) {
        var uid = getUidFromSign(signState);
        SignConfig.SignData data = signConfig.signs.get(uid);
        if (data == null) {
            Bukkit.getLogger().warning("sign data is null, skipped");
            return;
        }
        var boards = cacheManager.getLeaderBoard(data.item);
        var leaderBoard = leadersConfig.stats.get(data.item);
        if (leaderBoard == null) {
            plugin.getLogger().warning("cannot find leaderboard with " + data.item + ", skipped");
            return;
        }
        Optional<Board> boardOptional = Utils.getBoard(boards, data.rank);
        if (boardOptional.isEmpty()) {
            plugin.getLogger().warning("board is empty , skipped.");
            return;
        }
        Board board = boardOptional.get();
        if (board.getPlayerUUID() == null) {
            plugin.getLogger().warning("sign data is null, skipped.");
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
                plugin.getLogger().warning(headVector.toString() + " is not a player head");
                return;
            }
            boolean walled = Utils.isWalled(face.getOppositeFace(), head);
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
        plugin.getLogger().warning("sign data for ".concat(playerName).concat(" is updated."));
    }

    public void removeSign(Sign sign) {
        var uid = getUidFromSign(sign);
        signConfig.signs.remove(uid);
        try {
            signConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class LeaderSign {

        public final String item;
        public final int rank;

        private LeaderSign(String item, int rank) {
            this.item = item;
            this.rank = rank;
        }
    }
}

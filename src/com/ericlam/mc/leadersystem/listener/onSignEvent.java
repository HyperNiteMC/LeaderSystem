package com.ericlam.mc.leadersystem.listener;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.model.Board;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.TreeSet;

public class onSignEvent implements Listener {
    private final Plugin plugin;

    public onSignEvent(LeaderSystem plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onSignChanged(SignChangeEvent e){
        Block sign = e.getBlock();
        if (!(sign.getBlockData() instanceof WallSign)) return;
        Player player = e.getPlayer();
        String item = e.getLine(0);
        String rankStr = Optional.ofNullable(e.getLine(1)).orElse("");
        Utils.getItem(item).ifPresent(leaderBoard -> {
            int rank;
            try {
                rank = Integer.parseInt(rankStr);
            } catch (NumberFormatException ex) {
                player.sendMessage(LeaderConfig.notValue);
                return;
            }
            LeaderBoardManager leaderBoardManager = LeaderBoardManager.getInstance();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                TreeSet<Board> boards = leaderBoardManager.getRanking(leaderBoard);
                Utils.getBoard(boards, rank).ifPresentOrElse(board -> {
                    if (board.getPlayerUUID() == null) {
                        player.sendMessage(LeaderConfig.playerNull);
                        return;
                    }
                    player.sendMessage(LeaderConfig.createSignSuccess);
                    final double y = sign.getY();
                    final double x = sign.getX();
                    final double z = sign.getZ();
                    Block headBlock = new Location(player.getWorld(), x, y + 1, z).getBlock();
                    boolean walled = com.hypernite.mc.hnmc.core.utils.Utils.isWalled(headBlock);
                    if (!walled) {
                        Block signRelative = sign.getRelative(player.getFacing());
                        final double yR = signRelative.getY();
                        final double xR = signRelative.getX();
                        final double zR = signRelative.getZ();
                        headBlock = new Location(player.getWorld(), xR, yR + 1, zR).getBlock();
                    }
                    Location signLoc = sign.getLocation();
                    FileConfiguration signData = LeaderConfig.signData;
                    String uid = Utils.uidGenerator();
                    signData.set(uid + ".item", item);
                    signData.set(uid + ".rank", rank);
                    signData.set(uid + ".world", signLoc.getWorld().getName());
                    signData.set(uid + ".location.x", signLoc.getX());
                    signData.set(uid + ".location.y", signLoc.getY());
                    signData.set(uid + ".location.z", signLoc.getZ());
                    signData.set(uid + ".head-location.x", headBlock.getLocation().getX());
                    signData.set(uid + ".head-location.y", headBlock.getLocation().getY());
                    signData.set(uid + ".head-location.z", headBlock.getLocation().getZ());
                    LeaderConfig.saveSignData();
                    Block head = headBlock;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Sign signState = (Sign) sign.getState();
                        final String playerName = board.getPlayerName().equalsIgnoreCase("null") ? ChatColor.RED + "[! 找不到名稱]" : board.getPlayerName();
                        for (int i = 0; i < 4; i++) {
                            String line = leaderBoard.getSigns().get(i)
                                    .replaceAll("<rank>", board.getRank() + "")
                                    .replaceAll("<player>", playerName)
                                    .replaceAll("<data>", board.getDataShow());
                            signState.setLine(i, line);
                            e.setLine(i, line);
                        }
                        signState.update(true);
                        sign.getState().update(true);
                        if (board.getPlayerName().equalsIgnoreCase("null")) {
                            HyperNiteMC.getAPI().getPlayerSkinManager().setHeadBlock(board.getPlayerUUID(), head, walled, player.getFacing().getOppositeFace());
                        } else {
                            HyperNiteMC.getAPI().getPlayerSkinManager().setHeadBlock(board.getPlayerUUID(), board.getPlayerName(), head, walled, player.getFacing().getOppositeFace());
                        }
                    });
                }, () -> player.sendMessage(LeaderConfig.rankNull));
            });
        });
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getBlockData() instanceof WallSign)) return;
        Block sign = e.getBlock();
        Location signLoc = sign.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String uid = Utils.getUidFromLoc(signLoc);
            if (uid == null) return;
            FileConfiguration signData = LeaderConfig.signData;
            signData.set(uid, null);
            LeaderConfig.saveSignData();
            e.getPlayer().sendMessage(LeaderConfig.signRemoved);
        });
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getClickedBlock() == null || !(e.getClickedBlock().getBlockData() instanceof WallSign)) return;
        Player player = e.getPlayer();
        Block sign = e.getClickedBlock();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String uid = Utils.getUidFromLoc(sign.getLocation());
            if (uid == null) return;
            String item = LeaderConfig.signData.getString(uid + ".item");
            Utils.getItem(item).ifPresent(leaderBoard -> {
                Inventory inv = LeaderInventoryManager.getInstance().getLeaderInventory(leaderBoard);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.openInventory(inv);
                });
            });

        });

    }
}

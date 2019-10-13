package com.ericlam.mc.leadersystem.listener;

import com.ericlam.mc.leadersystem.config.LeaderConfigLegacy;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.ericlam.mc.leadersystem.sign.SignData;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

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
        if (e.getLines().length < 2) return;
        String item = e.getLine(0);
        String rankStr = Optional.ofNullable(e.getLine(1)).orElse("");
        Optional<LeaderBoard> leaderBoardOptional = Utils.getItem(item);
        if (leaderBoardOptional.isEmpty()) return;
        LeaderBoard leaderBoard = leaderBoardOptional.get();
        int rank;
        try {
            rank = Integer.parseInt(rankStr);
        } catch (NumberFormatException ex) {
            player.sendMessage(LeaderConfigLegacy.notValue);
            return;
        }
        LeaderSystem.getLeaderBoardManager().getRanking(leaderBoard).whenComplete((boards, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            //plugin.getLogger().warning(new Gson().toJson(boards));
            Optional<Board> boardOptional = Utils.getBoard(boards, rank);
            Board board;

            if (boardOptional.isEmpty()) {
                player.sendMessage(LeaderConfigLegacy.rankNull);
                board = new Board(rank, UUID.randomUUID(), "???", 9999, "???");
            } else {
                board = boardOptional.get();
            }

            if (board.getPlayerUUID() == null) {
                player.sendMessage(LeaderConfigLegacy.playerNull);
                return;
            }

            Vector headVector = sign.getLocation().add(0, 1, 0).toVector();
            Block headBlock = headVector.toLocation(sign.getWorld()).getBlock();
            boolean walled = com.hypernite.mc.hnmc.core.utils.Utils.isWalled(headBlock);
            if (!walled) {
                Block signRelative = sign.getRelative(player.getFacing());
                headVector = signRelative.getLocation().add(0, 1, 0).toVector();
                headBlock = headVector.toLocation(sign.getWorld()).getBlock();
            }
            final Block Head = headBlock;
            final String uid = headVector.toString().replaceAll("\\.0", "");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Sign signState = (Sign) sign.getState(false);
                LeaderConfigLegacy.signDataMap.put(signState, new SignData(leaderBoard.getItem(), uid, board.getRank(), sign.getWorld(), Head.getLocation().toVector().toBlockVector()));
                Utils.assignData(signState, boards, leaderBoard, player.getFacing().getOppositeFace());
                player.sendMessage(LeaderConfigLegacy.createSignSuccess);
            }, 10L);
            Utils.saveSignData(e.getBlock(), board, leaderBoard, headVector, uid);
        });
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) e.getBlock().getState(false);
        SignData signData = Utils.getSignData(sign);
        if (signData == null) return;
        Utils.removeSign(signData).whenComplete((v, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            e.getPlayer().sendMessage(LeaderConfigLegacy.signRemoved);
        });
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Sign)) return;
        Player player = e.getPlayer();
        Sign sign = (Sign) e.getClickedBlock().getState(false);
        SignData data = Utils.getSignData(sign);
        if (data == null) return;
        Utils.getItem(data.getItem()).ifPresent(leaderBoard -> LeaderSystem.getLeaderInventoryManager().getLeaderInventory(leaderBoard).whenComplete((inv, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        }));
    }
}

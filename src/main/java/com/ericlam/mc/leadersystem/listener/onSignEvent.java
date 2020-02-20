package com.ericlam.mc.leadersystem.listener;

import com.ericlam.mc.leadersystem.config.LangConfig;
import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
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

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class onSignEvent implements Listener {
    private final Plugin plugin;
    private LangConfig msg;
    private SignConfig signConfig;
    private LeadersConfig leadersConfig;

    public onSignEvent(LeaderSystem plugin) {
        this.plugin = plugin;
        msg = LeaderSystem.getYamlManager().getConfigAs(LangConfig.class);
        signConfig = LeaderSystem.getYamlManager().getConfigAs(SignConfig.class);
        leadersConfig = LeaderSystem.getYamlManager().getConfigAs(LeadersConfig.class);
    }


    @EventHandler
    public void onSignChanged(SignChangeEvent e) {
        Block sign = e.getBlock();
        if (!(sign.getBlockData() instanceof WallSign)) return;
        Player player = e.getPlayer();
        if (e.getLines().length < 2) return;
        String item = e.getLine(0);
        String rankStr = Optional.ofNullable(e.getLine(1)).orElse("");
        LeadersConfig.LeaderBoard leaderBoard = leadersConfig.stats.get(item);
        if (leaderBoard == null) return;
        int rank;
        try {
            rank = Integer.parseInt(rankStr);
        } catch (NumberFormatException ex) {
            player.sendMessage(msg.get("not-value"));
            return;
        }
        LeaderSystem.getLeaderBoardManager().getRanking(item).whenComplete((boards, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            if (boards.isEmpty()) {
                return;
            }
            Optional<Board> boardOptional = Utils.getBoard(boards, rank);
            Board board;

            if (boardOptional.isEmpty()) {
                player.sendMessage(msg.get("rank-null"));
                board = new Board(rank, UUID.randomUUID(), "???", 9999, "對方不在排名範圍內");
            } else {
                board = boardOptional.get();
            }

            if (board.getPlayerUUID() == null) {
                player.sendMessage(msg.get("player-null"));
                return;
            }

            Vector headVector = sign.getLocation().add(0, 1, 0).toVector();
            Block headBlock = headVector.toLocation(sign.getWorld()).getBlock();
            boolean walled = com.hypernite.mc.hnmc.core.utils.Utils.isWalled(headBlock);
            if (!walled) {
                Block signRelative = sign.getRelative(player.getFacing());
                headVector = signRelative.getLocation().add(0, 1, 0).toVector();
            }
            final String uid = Utils.vectorToUID(headVector);
            SignConfig.SignData data = new SignConfig.SignData();
            data.signLocation = sign.getLocation().toVector().toBlockVector();
            data.headLocation = headVector.toBlockVector();
            data.world = sign.getWorld().getName();
            data.item = item;
            data.rank = rank;
            signConfig.signs.put(uid, data);
            try {
                signConfig.save();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Sign signState = (Sign) sign.getState(false);
                Utils.assignData(signState, boards, leaderBoard, player.getFacing().getOppositeFace());
                player.sendMessage(msg.get("sign-create-success"));
            }, 10L);
        });
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) return;
        SignConfig.SignData signData = Utils.getSignData(e.getBlock().getLocation().toVector().toBlockVector());
        if (signData == null) return;
        Utils.removeSign(signData).whenComplete((v, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            e.getPlayer().sendMessage(msg.get("sign-removed"));
        });
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Sign)) return;
        Player player = e.getPlayer();
        SignConfig.SignData data = Utils.getSignData(e.getClickedBlock().getLocation().toVector().toBlockVector());
        if (data == null) return;
        Utils.getItem(data.item).ifPresent(leaderBoard -> LeaderSystem.getLeaderInventoryManager().getLeaderInventory(data.item, leaderBoard).whenComplete((inv, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        }));
    }
}

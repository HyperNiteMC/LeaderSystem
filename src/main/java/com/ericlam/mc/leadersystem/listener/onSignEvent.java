package com.ericlam.mc.leadersystem.listener;

import com.ericlam.mc.leadersystem.config.LangConfig;
import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.CacheManager;
import com.ericlam.mc.leadersystem.manager.SignManager;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;

public class onSignEvent implements Listener {
    private final Plugin plugin;
    private final LangConfig msg;
    private final SignConfig signConfig;
    private final LeadersConfig leadersConfig;
    private final CacheManager cacheManager;
    private final SignManager signManager;

    public onSignEvent(LeaderSystem plugin) {
        this.plugin = plugin;
        var yamlManager = plugin.getYamlManager();
        msg = yamlManager.getConfigAs(LangConfig.class);
        signConfig = yamlManager.getConfigAs(SignConfig.class);
        leadersConfig = yamlManager.getConfigAs(LeadersConfig.class);
        this.cacheManager = plugin.getCacheManager();
        this.signManager = plugin.getSignManager();
    }


    @EventHandler
    public void onSignChanged(SignChangeEvent e) {
        Block sign = e.getBlock();
        if (!(sign.getBlockData() instanceof WallSign)) return;
        Player player = e.getPlayer();
        Optional<SignManager.LeaderSign> leaderSignOptional;
        try {
            leaderSignOptional = signManager.parseSign(e.getLines());
        } catch (NumberFormatException ex) {
            player.sendMessage(msg.get("not-value"));
            return;
        }
        if (leaderSignOptional.isEmpty()) return;
        var leaderSign = leaderSignOptional.get();
        var boards = cacheManager.getLeaderBoard(leaderSign.item);

        Optional<Board> boardOptional = Utils.getBoard(boards, leaderSign.rank);

        Board board = boardOptional.orElseGet(() -> {
            player.sendMessage(msg.get("rank-null"));
            return new Board(leaderSign.rank, UUID.randomUUID(), "???", 0, "對方不在排名範圍內");
        });

        if (board.getPlayerUUID() == null) {
            player.sendMessage(msg.get("player-null"));
            return;
        }

        Sign signState = (Sign) sign.getState(false);
        signManager.updateSign(signState, player.getFacing().getOppositeFace());
        player.sendMessage(msg.get("sign-create-success"));

    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            signManager.removeSign((Sign) e.getBlock().getState());
            e.getPlayer().sendMessage(msg.get("sign-removed"));
        });
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Sign)) return;
        Player player = e.getPlayer();
        SignConfig.SignData data = signManager.getSignData((Sign) e.getClickedBlock().getState());
        if (data == null) return;
        Inventory inventory = cacheManager.getLeaderInventory(data.item);
        if (inventory == null) return;
        player.openInventory(inventory);
    }
}

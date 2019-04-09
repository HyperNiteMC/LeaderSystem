package com.ericlam.mc.leadersystem.listener;

import com.ericlam.mc.leadersystem.config.ConfigManager;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.hypernite.skin.PlayerHeadGetter;
import com.hypernite.skin.SkinDatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.TreeSet;

public class onSignEvent implements Listener {
    private final Plugin plugin;

    public onSignEvent(LeaderSystem plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onSignChanged(SignChangeEvent e){
        Block sign = e.getBlock();
        if (sign.getType() != Material.WALL_SIGN) return;
        Player player = e.getPlayer();
        String item = e.getLine(0);
        String rankStr = e.getLine(1);
        LeaderBoard leaderBoard = Utils.getItem(item);
        if (leaderBoard == null) return;
        int rank;
        try{
            rank = Integer.parseInt(rankStr);
        }catch (NumberFormatException ex){
            player.sendMessage(ConfigManager.notValue);
            return;
        }
        LeaderBoardManager leaderBoardManager = LeaderBoardManager.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            TreeSet<Board> boards = leaderBoardManager.getRanking(leaderBoard);
            Board board = Utils.getBoard(boards, rank);
            if (board == null){
                player.sendMessage(ConfigManager.rankNull);
                return;
            }
            if (board.getPlayerName()==null || board.getPlayerUUID()==null){
                player.sendMessage(ConfigManager.playerNull);
                return;
            }
            player.sendMessage(ConfigManager.createSignSuccess);
            String base64 = SkinDatabaseManager.getInstance().getPlayerSkin(board.getPlayerUUID(), board.getPlayerName());
            Bukkit.getScheduler().runTask(plugin,()->{
                if (sign.getType() != Material.WALL_SIGN) sign.setType(Material.WALL_SIGN);
                Sign signState = (Sign) sign.getState();
                for (int i = 0; i < 4; i++) {
                    String line = leaderBoard.getSigns().get(i)
                            .replaceAll("<rank>",board.getRank()+"")
                            .replaceAll("<player>",board.getPlayerName())
                            .replaceAll("<data>",board.getDataShow());
                    signState.setLine(i,line);
                    e.setLine(i,line);
                }
                signState.update(true);
                sign.getState().update(true);
                final double y  = sign.getY();
                final double x = sign.getX();
                final double z = sign.getZ();
                Block headBlock = new Location(player.getWorld(),x,y+1,z).getBlock();
                boolean walled = com.ericlam.utils.Utils.isWalled(headBlock);
                if (!walled) {
                    Block signRelative = sign.getRelative(player.getFacing());
                    final double yR = signRelative.getY();
                    final double xR = signRelative.getX();
                    final double zR = signRelative.getZ();
                    headBlock = new Location(player.getWorld(), xR, yR + 1, zR).getBlock();
                }
                PlayerHeadGetter.setHeadBlock(base64,headBlock,walled,player.getFacing().getOppositeFace());
                Location signLoc = sign.getLocation();
                FileConfiguration signData = ConfigManager.signData;
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
                ConfigManager.saveSignData();
            });
        });
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.WALL_SIGN) return;
        Block sign = e.getBlock();
        Location signLoc = sign.getLocation();
        String uid = Utils.getUidFromLoc(signLoc);
        if (uid == null) return;
        FileConfiguration signData = ConfigManager.signData;
        signData.set(uid, null);
        ConfigManager.saveSignData();
        e.getPlayer().sendMessage(ConfigManager.signRemoved);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.WALL_SIGN) return;
        Player player = e.getPlayer();
        Block sign = e.getClickedBlock();
        String uid = Utils.getUidFromLoc(sign.getLocation());
        if (uid == null) return;
        String item = ConfigManager.signData.getString(uid + ".item");
        LeaderBoard leaderBoard = Utils.getItem(item);
        if (leaderBoard == null) return;
        player.openInventory(LeaderInventoryManager.getInstance().getLeaderInventory(leaderBoard));
    }
}

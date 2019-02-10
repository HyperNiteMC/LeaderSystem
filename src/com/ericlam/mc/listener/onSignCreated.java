package com.ericlam.mc.listener;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.main.LeaderSystem;
import com.ericlam.mc.main.Utils;
import com.ericlam.mc.manager.LeaderBoardManager;
import com.ericlam.mc.model.Board;
import com.ericlam.mc.model.LeaderBoard;
import com.hypernite.skin.PlayerHeadGetter;
import com.hypernite.skin.SkinDatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class onSignCreated implements Listener {
    private final Plugin plugin;

    public onSignCreated(LeaderSystem plugin){
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
            List<Board> boards = leaderBoardManager.getRanking(leaderBoard);
            Board board = Utils.getBoardFromRank(boards,rank);
            if (board == null){
                player.sendMessage(ConfigManager.rankNull);
                return;
            }
            if (board.getPlayerName()==null || board.getPlayerUUID()==null){
                player.sendMessage(ConfigManager.playerNull);
                return;
            }
            String base64 = SkinDatabaseManager.getInstance().getPlayerSkin(board.getPlayerUUID());
            Bukkit.getScheduler().runTask(plugin,()->{
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
                if (!walled) headBlock = sign.getRelative(player.getFacing());
                PlayerHeadGetter.setHeadBlock(base64,headBlock,walled,player.getFacing().getOppositeFace());
                player.sendMessage(ConfigManager.createSignSuccess);
            });
        });
    }
}

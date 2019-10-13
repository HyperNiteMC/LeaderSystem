package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeaderConfigLegacy;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.hypernite.mc.hnmc.core.builders.InventoryBuilder;
import com.hypernite.mc.hnmc.core.builders.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LeaderInventoryManager {
    private LeaderBoardManager leaderBoardManager;
    private HashMap<String, Inventory> leaderInventories = new HashMap<>();
    private Plugin plugin;

    public LeaderInventoryManager(LeaderBoardManager leaderBoardManager, Plugin plugin) {
        this.leaderBoardManager = leaderBoardManager;
        this.plugin = plugin;
    }

    public HashMap<String, Inventory> getLeaderInventories() {
        return leaderInventories;
    }

    private String replaceData(String str, Board board) {
        return str.replaceAll("<player>", board.getPlayerName()).replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow());
    }

    public void clearCache() {
        leaderInventories.clear();
    }

    @Deprecated
    public CompletableFuture<Void> loadLeaderBoards() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        LeaderConfigLegacy.leaderBoards.forEach(leaderBoard -> {
            if (leaderInventories.containsKey(leaderBoard.getItem())) return;
            future.thenCombineAsync(leaderBoardManager.getRanking(leaderBoard), (v, e) -> e);
        });
        return future;
    }

    private CompletableFuture<Inventory> getLeaderInventoryFromSQL(LeaderBoard leaderBoard) {
        String item = leaderBoard.getItem();
        Inventory inv = new InventoryBuilder(LeaderConfigLegacy.guiRow, leaderBoard.getInvTitle()).build();
        return leaderBoardManager.getRanking(leaderBoard).thenApply(treeSet -> {
            LinkedList<Board> boards = new LinkedList<>(treeSet);
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < (LeaderConfigLegacy.guiRow * 9); i++) {
                    if (boards.size() <= i) break;
                    Board board = boards.get(i);
                    if (board.getPlayerUUID() == null) continue;
                    String invName = replaceData(leaderBoard.getInvName(), board);
                    ItemStackBuilder stackBuilder = new ItemStackBuilder(Material.PLAYER_HEAD);
                    if (board.getPlayerName().equalsIgnoreCase("null")) {
                        stackBuilder.head(board.getPlayerUUID()).displayName(ChatColor.RED + "[! 找不到名稱]");
                    } else {
                        stackBuilder.head(board.getPlayerUUID(), board.getPlayerName()).displayName(invName);
                    }
                    stackBuilder.lore(leaderBoard.getLores().stream().map(line -> replaceData(line, board)).collect(Collectors.toList())).onClick(e -> e.setCancelled(true));
                    inv.setItem(i, stackBuilder.build());
                }
            });
            leaderInventories.put(item, inv);
            return inv;
        });
    }


    public CompletableFuture<Inventory> getLeaderInventory(LeaderBoard leaderBoard) {
        String item = leaderBoard.getItem();
        if (leaderInventories.containsKey(item)) return CompletableFuture.completedFuture(leaderInventories.get(item));
        else return getLeaderInventoryFromSQL(leaderBoard);
    }


}

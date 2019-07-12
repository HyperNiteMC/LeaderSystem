package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.hypernite.mc.hnmc.core.builders.InventoryBuilder;
import com.hypernite.mc.hnmc.core.builders.ItemStackBuilder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class LeaderInventoryManager {
    private static LeaderInventoryManager leaderInventoryManager;
    private LeaderBoardManager leaderBoardManager;
    private HashMap<String, Inventory> leaderInventories = new HashMap<>();
    private BukkitTask updateTask;

    private LeaderInventoryManager() {
        leaderBoardManager = LeaderBoardManager.getInstance();
    }

    public static LeaderInventoryManager getInstance() {
        if (leaderInventoryManager == null) leaderInventoryManager = new LeaderInventoryManager();
        return leaderInventoryManager;
    }

    public HashMap<String, Inventory> getLeaderInventories() {
        return leaderInventories;
    }

    private String replaceData(String str, Board board) {
        return str.replaceAll("<player>", board.getPlayerName()).replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow());
    }

    private Inventory getLeaderInventoryFromSQL(Connection connection, LeaderBoard leaderBoard) throws SQLException {
        String item = leaderBoard.getItem();
        Inventory inv = new InventoryBuilder(LeaderConfig.guiRow, leaderBoard.getInvTitle()).build();
        List<Board> boards = new ArrayList<>(leaderBoardManager.getRanking(connection, leaderBoard));
        for (int i = 0; i < (LeaderConfig.guiRow * 9); i++) {
            if (boards.size() <= i) break;
            Board board = boards.get(i);
            if (board.getPlayerUUID() == null) continue;
            String invName = replaceData(leaderBoard.getInvName(), board);
            ItemStackBuilder stackBuilder = new ItemStackBuilder(Material.PLAYER_HEAD).displayName(invName);
            if (board.getPlayerName().equalsIgnoreCase(ChatColor.RED + "Name Not Found")) {
                stackBuilder.head(board.getPlayerUUID());
            } else {
                stackBuilder.head(board.getPlayerUUID(), board.getPlayerName());
            }
            stackBuilder.lore(leaderBoard.getLores().stream().map(line -> replaceData(line, board)).collect(Collectors.toList())).onClick(e -> e.setCancelled(true));
            inv.setItem(i, stackBuilder.build());
        }
        leaderInventories.put(item, inv);
        return inv;
    }

    private Inventory getLeaderInventoryFromSQL(LeaderBoard leaderBoard) {
        try (Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()) {
            return this.getLeaderInventoryFromSQL(connection, leaderBoard);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new InventoryBuilder(LeaderConfig.guiRow, leaderBoard.getInvTitle()).build();
    }


    public Inventory getLeaderInventory(LeaderBoard leaderBoard) {
        String item = leaderBoard.getItem();
        if (leaderInventories.containsKey(item)) return leaderInventories.get(item);
        else return getLeaderInventoryFromSQL(leaderBoard);
    }


    public void forceUpdateInv() {
        ConcurrentLinkedDeque<String> itemQueue = new ConcurrentLinkedDeque<>(leaderInventories.keySet());
        try (Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()) {
            while (!itemQueue.isEmpty()) {
                String item = itemQueue.poll();
                if (item == null) continue;
                Optional<LeaderBoard> leaderBoard = Utils.getItem(item);
                if (leaderBoard.isPresent()) this.getLeaderInventoryFromSQL(connection, leaderBoard.get());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void inventoryUpdateScheduler(Plugin plugin) {
        if (updateTask != null) return;
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                forceUpdateInv();
                plugin.getLogger().info("Leader Inventories Updated.");
            }
        }.runTaskTimerAsynchronously(plugin, 3600 * 20L, 3600 * 20L);
    }


}

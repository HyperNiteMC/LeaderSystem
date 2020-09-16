package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.MainConfig;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.runnables.LeaderBoardNonExistException;
import com.hypernite.mc.hnmc.core.builders.InventoryBuilder;
import com.hypernite.mc.hnmc.core.builders.ItemStackBuilder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class LoadManager {

    private final LeadersConfig leadersConfig;
    private final MainConfig mainConfig;
    private final SQLDataSource sqlDataSource;

    public LoadManager(YamlManager yamlManager) {
        this.leadersConfig = yamlManager.getConfigAs(LeadersConfig.class);
        this.mainConfig = yamlManager.getConfigAs(MainConfig.class);
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
    }

    public TreeSet<Board> getRankingFromSQL(String item) throws LeaderBoardNonExistException {
        if (!leadersConfig.stats.containsKey(item)) throw new LeaderBoardNonExistException(item);
        LeadersConfig.LeaderBoard leaderBoard = leadersConfig.stats.get(item);
        TreeSet<Board> boards = new TreeSet<>();
        String table = leaderBoard.table;
        String column = leaderBoard.column;
        String name = leaderBoard.playerName;
        String uuid = leaderBoard.playerUuid;
        String show = leaderBoard.dataShow;
        int limit = mainConfig.selectLimit;
        String a = name.isEmpty() ? "" : String.format(", `%s`", name);
        String b = show.isEmpty() ? column : show;
        final String stmt = "SELECT `" + uuid + "`" + a + ", `" + b + "` FROM " + table + " ORDER BY `" + column + "` DESC LIMIT " + limit;
        try (Connection connection = sqlDataSource.getConnection();
             PreparedStatement select = connection.prepareStatement(stmt)) {
            ResultSet resultSet = select.executeQuery();
            int i = 1;
            while (resultSet.next()) {
                String playername = name.isEmpty() ? "" : resultSet.getString(name);
                UUID playeruuid = UUID.fromString(resultSet.getString(uuid));
                String datashow = show.isEmpty() ? "" : resultSet.getString(show);
                int data = resultSet.getInt(column);
                boards.add(new Board(i, playeruuid, playername, data, datashow));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boards;
    }

    public Inventory loadLeaderInventory(String item, TreeSet<Board> treeSet) {
        LeadersConfig.LeaderBoard leaderBoard = leadersConfig.stats.get(item);
        Inventory inv = new InventoryBuilder(mainConfig.guiRow, leaderBoard.invTitle).build();
        LinkedList<Board> boards = new LinkedList<>(treeSet);
        for (int i = 0; i < (mainConfig.guiRow * 9); i++) {
            if (boards.size() <= i) break;
            Board board = boards.get(i);
            if (board.getPlayerUUID() == null) continue;
            String invName = replaceData(leaderBoard.invName, board);
            ItemStackBuilder stackBuilder = new ItemStackBuilder(Material.PLAYER_HEAD);
            if (board.getPlayerName().equalsIgnoreCase("null")) {
                stackBuilder.head(board.getPlayerUUID()).displayName(ChatColor.RED + "[! 找不到名稱]");
            } else {
                stackBuilder.head(board.getPlayerUUID(), board.getPlayerName()).displayName(invName);
            }
            stackBuilder.lore(leaderBoard.invLores.stream().map(line -> replaceData(line, board)).collect(Collectors.toList())).onClick(e -> e.setCancelled(true));
            inv.setItem(i, stackBuilder.build());
        }
        return inv;
    }

    private String replaceData(String str, Board board) {
        return str.replaceAll("<player>", board.getPlayerName()).replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow());
    }


}

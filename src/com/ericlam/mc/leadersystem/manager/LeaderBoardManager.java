package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderBoardManager {
    private Map<String, TreeSet<Board>> caching = new ConcurrentHashMap<>();
    private SQLDataSource sqlDataSource;

    public LeaderBoardManager() {
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
    }

    public CompletableFuture<TreeSet<Board>> getRanking(LeaderBoard leaderBoard) {
        String item = leaderBoard.getItem();
        if (caching.containsKey(item)) return CompletableFuture.completedFuture(caching.get(item));
        else return this.getRankingFromSQL(leaderBoard);
    }

    private CompletableFuture<TreeSet<Board>> getRankingFromSQL(LeaderBoard leaderBoard) {
        String origDatabase = HyperNiteMC.getAPI().getCoreConfig().getDataBase().getString("database");
        TreeSet<Board> boards = new TreeSet<>();
        String database = leaderBoard.getDatabase();
        String table = leaderBoard.getTable();
        String column = leaderBoard.getColumn();
        String name = leaderBoard.getPlayername();
        String uuid = leaderBoard.getPlayeruuid();
        String show = leaderBoard.getDatashow();
        int limit = LeaderConfig.selectLimit;
        String a = name.isEmpty() ? "" : String.format(", `%s`", name);
        String b = show.isEmpty() ? column : show;
        final String stmt = "SELECT `" + uuid + "`" + a + ", `" + b + "` FROM " + table + " ORDER BY `" + column + "` DESC LIMIT " + limit;
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection();
                 PreparedStatement use = connection.prepareStatement("USE " + database);
                 PreparedStatement select = connection.prepareStatement(stmt);
                 PreparedStatement back = connection.prepareStatement("USE " + origDatabase)) {
                use.execute();
                ResultSet resultSet = select.executeQuery();
                back.execute();
                int i = 1;
                while (resultSet.next()) {
                    String playername = name.isEmpty() ? "" : resultSet.getString(name);
                    UUID playeruuid = UUID.fromString(resultSet.getString(uuid));
                    String datashow = show.isEmpty() ? "" : resultSet.getString(show);
                    int data = resultSet.getInt(column);
                    boards.add(new Board(i, playeruuid, playername, data, datashow));
                    i++;
                }
                caching.put(leaderBoard.getItem(), boards);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return boards;
        });
    }

    public Map<String, TreeSet<Board>> getCaching() {
        return caching;
    }

    public void clearCache() {
        caching.clear();
    }

    public void updateSignData() {
        LeaderConfig.signDataMap.forEach((k, v) -> {
            TreeSet<Board> boards = caching.get(v.getItem());
            if (boards == null) return;
            Utils.getItem(v.getItem()).ifPresent(leaderBoard -> Utils.assignData(k, boards, leaderBoard));
        });
    }
}

package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.MainConfig;
import com.ericlam.mc.leadersystem.config.SignConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
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
    private final Map<String, TreeSet<Board>> caching = new ConcurrentHashMap<>();
    private final SQLDataSource sqlDataSource;
    private final LeadersConfig leadersConfig;
    private final MainConfig mainConfig;
    private final SignConfig signConfig;

    public LeaderBoardManager() {
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
        this.leadersConfig = LeaderSystem.getYamlManager().getConfigAs(LeadersConfig.class);
        this.mainConfig = LeaderSystem.getYamlManager().getConfigAs(MainConfig.class);
        this.signConfig = LeaderSystem.getYamlManager().getConfigAs(SignConfig.class);
    }

    public CompletableFuture<TreeSet<Board>> getRanking(String item) {
        if (caching.containsKey(item)) return CompletableFuture.completedFuture(caching.get(item));
        else return getRankingFromSQL(item);

    }

    private CompletableFuture<TreeSet<Board>> getRankingFromSQL(String item) {
        if (!leadersConfig.stats.containsKey(item)) return CompletableFuture.completedFuture(new TreeSet<>());
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
        return CompletableFuture.supplyAsync(() -> {
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
                caching.put(item, boards);
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
        signConfig.signs.values().forEach(data -> {
            var boards = caching.get(data.item);
            if (boards == null) return;
            var state = Utils.getState(data);
            if (state == null) return;
            Utils.getItem(data.item).ifPresent(l -> Utils.assignData(state, boards, l, null));
        });
    }
}

package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.config.LeaderConfig;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.model.Board;
import com.ericlam.mc.leadersystem.model.LeaderBoard;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LeaderBoardManager {
    private static LeaderBoardManager leaderBoardManager;

    public static LeaderBoardManager getInstance() {
        if (leaderBoardManager == null) leaderBoardManager = new LeaderBoardManager();
        return leaderBoardManager;
    }

    private HashMap<String, TreeSet<Board>> caching = new HashMap<>();
    private Set<LeaderBoard> usingLeaderBoards = new HashSet<>();
    private BukkitTask updateTask, signUpdateTask;

    public TreeSet<Board> getRanking(LeaderBoard leaderBoard) {
        String item = leaderBoard.getItem();
        if (caching.containsKey(item)) return caching.get(item);
        else return getRankingFromSQL(leaderBoard);
    }

    TreeSet<Board> getRanking(Connection connection, LeaderBoard leaderBoard) throws SQLException {
        String item = leaderBoard.getItem();
        if (caching.containsKey(item)) return caching.get(item);
        else return getRankingFromSQL(connection, leaderBoard);
    }

    public Set<LeaderBoard> getUsingLeaderBoards() {
        return usingLeaderBoards;
    }

    private TreeSet<Board> getRankingFromSQL(LeaderBoard leaderBoard) {
        try (Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()) {
            return getRankingFromSQL(connection, leaderBoard);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TreeSet<>();
    }

    private TreeSet<Board> getRankingFromSQL(Connection connection, LeaderBoard leaderBoard) throws SQLException {
        usingLeaderBoards.add(leaderBoard);
        String origDatabase = HyperNiteMC.getAPI().getCoreConfig().getDataBase().getString("database");
        TreeSet<Board> boards = new TreeSet<>();
        String database = leaderBoard.getDatabase();
        String table = leaderBoard.getTable();
        String column = leaderBoard.getColumn();
        String name = leaderBoard.getPlayername();
        String uuid = leaderBoard.getPlayeruuid();
        String show = leaderBoard.getDatashow();
        int limit = LeaderConfig.selectLimit;
        String selectStmt;
        selectStmt = "SELECT "+(name.isEmpty() ? "" : "`"+name+"`,")+"`"+uuid+"`,`"+(show.isEmpty() ? column : show)+"` FROM "+table+" ORDER BY "+column+" DESC LIMIT "+limit;
        PreparedStatement use = connection.prepareStatement("USE " + database);
        PreparedStatement select = connection.prepareStatement(selectStmt);
        PreparedStatement back = connection.prepareStatement("USE " + origDatabase);
            use.execute();
            ResultSet resultSet = select.executeQuery();
            back.execute();
            int i = 1;
            while (resultSet.next()){
                String playername = name.isEmpty() ? "" : resultSet.getString(name);
                UUID playeruuid = UUID.fromString(resultSet.getString(uuid));
                String datashow = show.isEmpty() ? "" : resultSet.getString(show);
                int data = resultSet.getInt(column);
                boards.add(new Board(i,playeruuid,playername,data,datashow));
                i++;
            }
            caching.put(leaderBoard.getItem(),boards);
            return boards;
    }

    public void forceUpdateSQL() {
        ConcurrentLinkedDeque<LeaderBoard> leaderBoards = new ConcurrentLinkedDeque<>(usingLeaderBoards);
        try (Connection connection = HyperNiteMC.getAPI().getSQLDataSource().getConnection()) {
            while (!leaderBoards.isEmpty()) {
                LeaderBoard leaderBoard = leaderBoards.poll();
                getRankingFromSQL(connection, leaderBoard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startUpdateScheduler(Plugin plugin) {
        if (updateTask != null) return;
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                forceUpdateSQL();
                plugin.getLogger().info("Leader Board updated.");
            }
        }.runTaskTimerAsynchronously(plugin, 3600 * 20L, 3600 * 20L);
    }

    public void forceUpdateSigns(Plugin plugin) {
        FileConfiguration signData = LeaderConfig.signData;
        ConcurrentLinkedDeque<String> signDataLinked = new ConcurrentLinkedDeque<>(signData.getKeys(false));
        while (!signDataLinked.isEmpty()) {
            String uid = signDataLinked.poll();
            String item = signData.getString(uid + ".item");
            int rank = signData.getInt(uid + ".rank");
            Location loc = Utils.getLocationFromConfig(signData, uid);
            Location headLoc = Utils.getLocationFromConfig(signData, uid, "head-location");
            if (loc == null || headLoc == null) continue;
            if (loc.getBlock().getBlockData() instanceof WallSign) continue;
            Block sign = loc.getBlock();
            Utils.getItem(item).ifPresent(leaderBoard -> {
                TreeSet<Board> boards = getRanking(leaderBoard);
                Utils.getBoard(boards, rank).ifPresent(board -> {
                    if (board.getPlayerUUID() == null || board.getPlayerName() == null) return;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Sign signState = (Sign) sign.getState();
                        for (int i = 0; i < 4; i++) {
                            String line = leaderBoard.getSigns().get(i)
                                    .replaceAll("<rank>", board.getRank() + "")
                                    .replaceAll("<player>", board.getPlayerName())
                                    .replaceAll("<data>", board.getDataShow());
                            signState.setLine(i, line);
                        }
                        signState.update(true);
                        sign.getState().update(true);
                        Block headBlock = headLoc.getBlock();
                        HyperNiteMC.getAPI().getPlayerSkinManager().updateHeadBlock(board.getPlayerUUID(), board.getPlayerName(), headBlock);
                    });
                });
                });
            }
    }

    public void startSignUpdate(Plugin plugin) {
        if (signUpdateTask != null) return;
        signUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                forceUpdateSigns(plugin);
                plugin.getLogger().info("Leader Signs Updated.");
            }
        }.runTaskTimerAsynchronously(plugin, 3600 * 20L, 3600 * 20L);
    }
}

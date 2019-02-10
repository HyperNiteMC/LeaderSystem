package com.ericlam.mc.manager;

import com.ericlam.mc.config.ConfigManager;
import com.ericlam.mc.main.LeaderSystem;
import com.ericlam.mc.model.Board;
import com.ericlam.mc.model.LeaderBoard;
import com.hypernite.mysql.SQLDataSourceManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LeaderBoardManager {
    private static LeaderBoardManager leaderBoardManager;

    public static LeaderBoardManager getInstance() {
        if (leaderBoardManager == null) leaderBoardManager = new LeaderBoardManager();
        return leaderBoardManager;
    }

    private HashMap<String,List<Board>> caching = new HashMap<>();
    private BukkitTask task;

    public List<Board> getRanking(LeaderBoard leaderBoard){
        String item = leaderBoard.getItem();
        if (caching.containsKey(item)) return caching.get(item);
        else return getRankingFromSQL(leaderBoard);
    }

    private List<Board> getRankingFromSQL(LeaderBoard leaderBoard){
        List<Board> boards = new ArrayList<>();
        String database = leaderBoard.getDatabase();
        String table = leaderBoard.getTable();
        String column = leaderBoard.getColumn();
        String name = leaderBoard.getPlayername();
        String uuid = leaderBoard.getPlayeruuid();
        String show = leaderBoard.getDatashow();
        int limit = leaderBoard.getLimit();
        String selectStmt;
        selectStmt = "SELECT "+(name.isEmpty() ? "" : "`"+name+"`,")+"`"+uuid+"`,`"+(show.isEmpty() ? column : show)+"` FROM "+table+" ORDER BY "+column+" DESC LIMIT "+limit;
        try(Connection connection = SQLDataSourceManager.getInstance().getFuckingConnection();
            PreparedStatement use = connection.prepareStatement("USE "+database);
            PreparedStatement select = connection.prepareStatement(selectStmt)){
            use.execute();
            ResultSet resultSet = select.executeQuery();
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
        } catch (SQLException e) {
            e.printStackTrace();
            return boards;
        }
    }

    public synchronized void startUpdateScheduler(){
        if (task !=null) return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<LeaderBoard> leaderBoardIterator = ConfigManager.leaderBoards.iterator();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (leaderBoardIterator.hasNext()){
                            LeaderBoard leaderBoard = leaderBoardIterator.next();
                            getRankingFromSQL(leaderBoard);
                        }else{
                            cancel();
                        }
                    }
                }.runTaskTimerAsynchronously(LeaderSystem.plugin,0L,10 * 20L);
            }
        }.runTaskTimerAsynchronously(LeaderSystem.plugin,300 * 20L , 3600 *20L);
    }
}

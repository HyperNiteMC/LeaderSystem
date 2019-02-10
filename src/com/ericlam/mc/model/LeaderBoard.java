package com.ericlam.mc.model;

import java.util.List;

public class LeaderBoard {
    private String item,database,table,column,playername,playeruuid,datashow;
    private int limit;
    private List<String> signs,lores;

    public LeaderBoard(String item, String database, String table, String column, String playername, String playeruuid, String datashow, int limit, List<String> signs, List<String> lores) {
        this.item = item;
        this.database = database;
        this.table = table;
        this.column = column;
        this.playername = playername;
        this.playeruuid = playeruuid;
        this.datashow = datashow;
        this.limit = limit;
        this.signs = signs;
        this.lores = lores;
    }

    public List<String> getSigns() {
        return signs;
    }

    public List<String> getLores() {
        return lores;
    }

    public String getItem() {
        return item;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public String getPlayername() {
        return playername;
    }

    public String getPlayeruuid() {
        return playeruuid;
    }

    public String getDatashow() {
        return datashow;
    }

    public int getLimit() {
        return limit;
    }
}

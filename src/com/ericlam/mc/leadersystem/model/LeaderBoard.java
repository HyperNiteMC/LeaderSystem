package com.ericlam.mc.leadersystem.model;

import java.util.List;

public class LeaderBoard {
    private String item;
    private String database;
    private String table;
    private String column;
    private String playername;
    private String playeruuid;
    private String datashow;
    private String invTitle;
    private String invName;
    private List<String> signs,lores;

    public LeaderBoard(String item, String database, String table, String column, String playername, String playeruuid, String datashow, List<String> signs, List<String> lores, String invTitle, String invName) {
        this.item = item;
        this.database = database;
        this.table = table;
        this.column = column;
        this.playername = playername;
        this.playeruuid = playeruuid;
        this.datashow = datashow;
        this.signs = signs;
        this.lores = lores;
        this.invTitle = invTitle;
        this.invName = invName;
    }

    public String getInvTitle() {
        return invTitle;
    }

    public String getInvName() {
        return invName;
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
}

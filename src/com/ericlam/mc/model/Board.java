package com.ericlam.mc.model;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class Board {
    private int rank;
    private UUID playerUUID;
    private String playerName;
    private String dataShow;

    public Board(int rank, UUID playerUUID, String playerName, int data, String dataShow) {
        this.rank = rank;
        this.playerUUID = playerUUID;
        if (playerName.isEmpty()){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            if (offlinePlayer==null) {
                this.playerUUID = null;
                this.playerName = null;
            }else{
                this.playerName = offlinePlayer.getName();
            }
        }else{
            this.playerName = playerName;
        }
        this.dataShow = dataShow.isEmpty() ? data+"" : dataShow;
    }

    public int getRank() {
        return rank;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDataShow() {
        return dataShow;
    }
}

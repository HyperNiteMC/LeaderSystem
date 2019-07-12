package com.ericlam.mc.leadersystem.model;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public class Board implements Comparable<Board> {
    private int rank;
    private UUID playerUUID;
    private String playerName;
    private String dataShow;

    public Board(int rank, UUID playerUUID, String playerName, int data, String dataShow) {
        this.rank = rank;
        this.playerUUID = playerUUID;
        if (playerName.isEmpty()){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            this.playerName = Optional.ofNullable(offlinePlayer.getName()).orElse("null");
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

    @Override
    public int compareTo(@Nonnull Board board) {
        return Integer.compare(this.rank, board.getRank());
    }
}

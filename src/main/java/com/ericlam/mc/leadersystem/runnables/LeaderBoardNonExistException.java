package com.ericlam.mc.leadersystem.runnables;

public class LeaderBoardNonExistException extends Exception {

    private final String item;

    public LeaderBoardNonExistException(String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }
}

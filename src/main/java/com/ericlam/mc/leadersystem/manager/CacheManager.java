package com.ericlam.mc.leadersystem.manager;

import com.ericlam.mc.leadersystem.model.Board;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private final Map<String, TreeSet<Board>> leaderBoards = new ConcurrentHashMap<>();
    private final Map<String, Inventory> leaderInventories = new ConcurrentHashMap<>();

    public void setLeaderBoard(String item, TreeSet<Board> treeSet) {
        this.leaderBoards.put(item, treeSet);
    }

    public void setLeaderInventory(String item, Inventory inventory) {
        this.leaderInventories.put(item, inventory);
    }

    public Inventory getLeaderInventory(String item) {
        return leaderInventories.get(item);
    }

    public TreeSet<Board> getLeaderBoard(String item) {
        return leaderBoards.get(item);
    }

    public void clearLeaderBoards() {
        leaderBoards.clear();
    }

    public void clearLeaderInventories() {
        leaderInventories.clear();
    }
}

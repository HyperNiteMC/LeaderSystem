package com.ericlam.mc.listener;

import com.ericlam.mc.manager.LeaderInventoryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class onInventoryEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (LeaderInventoryManager.getInstance().getLeaderInventories().containsValue(inventory)) e.setCancelled(true);
    }
}

package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.commandhandler.LeaderSystemCommand;
import com.ericlam.mc.leadersystem.config.*;
import com.ericlam.mc.leadersystem.listener.onSignEvent;
import com.ericlam.mc.leadersystem.manager.LeaderBoardManager;
import com.ericlam.mc.leadersystem.manager.LeaderInventoryManager;
import com.ericlam.mc.leadersystem.placeholders.PlaceholderHook;
import com.ericlam.mc.leadersystem.runnables.DataUpdateRunnable;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import com.hypernite.mc.hnmc.core.utils.Tools;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderSystem extends JavaPlugin {

    private static YamlManager yamlManager;
    private static LeaderBoardManager leaderBoardManager;
    private static LeaderInventoryManager leaderInventoryManager;
    private Runnable clearCache;

    public static YamlManager getYamlManager() {
        return yamlManager;
    }

    public static LeaderBoardManager getLeaderBoardManager() {
        return leaderBoardManager;
    }

    public static LeaderInventoryManager getLeaderInventoryManager() {
        return leaderInventoryManager;
    }

    public Runnable getClearCache() {
        return clearCache;
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(SignVector.class);
        yamlManager = HyperNiteMC.getAPI().getFactory().getConfigFactory(this)
                .register("config.yml", MainConfig.class)
                .register("lang.yml", LangConfig.class)
                .register("leaders.yml", LeadersConfig.class)
                .register("signs.yml", SignConfig.class).dump();

        this.getServer().getPluginManager().registerEvents(new onSignEvent(this), this);
        leaderBoardManager = new LeaderBoardManager();
        leaderInventoryManager = new LeaderInventoryManager(leaderBoardManager, this);
        clearCache = () -> {
            leaderBoardManager.clearCache();
            leaderInventoryManager.clearCache();
        };
        new DataUpdateRunnable(this).runTaskTimer(this, Tools.randomWithRange(1200, 3600) * 20L, 3600 * 20L);
        new LeaderSystemCommand(this).register();
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Found PlaceholderAPI! registering placeholders...");
            new PlaceholderHook(this).register();
        }
        this.getLogger().info("LeaderSystem Enabled.");
    }
}

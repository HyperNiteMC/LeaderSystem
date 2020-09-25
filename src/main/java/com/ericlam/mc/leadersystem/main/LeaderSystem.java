package com.ericlam.mc.leadersystem.main;

import com.ericlam.mc.leadersystem.commandhandler.LeaderSystemCommand;
import com.ericlam.mc.leadersystem.config.*;
import com.ericlam.mc.leadersystem.listener.onSignEvent;
import com.ericlam.mc.leadersystem.manager.CacheManager;
import com.ericlam.mc.leadersystem.manager.LoadManager;
import com.ericlam.mc.leadersystem.manager.SignManager;
import com.ericlam.mc.leadersystem.placeholders.PlaceholderHook;
import com.ericlam.mc.leadersystem.runnables.ScheduleUpdateRunnable;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LeaderSystem extends JavaPlugin {

    private YamlManager yamlManager;
    private CacheManager cacheManager;
    private LoadManager loadManager;
    private SignManager signManager;
    private final ConcurrentLinkedQueue<String> itemQueue = new ConcurrentLinkedQueue<>();

    public YamlManager getYamlManager() {
        return yamlManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public LoadManager getLoadManager() {
        return loadManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    public ConcurrentLinkedQueue<String> getItemQueue() {
        return itemQueue;
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(SignVector.class);
        yamlManager = HyperNiteMC.getAPI().getFactory().getConfigFactory(this)
                .register("config.yml", MainConfig.class)
                .register("lang.yml", LangConfig.class)
                .register("leaders.yml", LeadersConfig.class)
                .register("signs.yml", SignConfig.class).dump();
        cacheManager = new CacheManager();
        loadManager = new LoadManager(yamlManager);
        signManager = new SignManager(this);

        this.getServer().getPluginManager().registerEvents(new onSignEvent(this), this);

        new ScheduleUpdateRunnable(this).runTaskTimer(this, 20L, 1200 * 20L);

        new LeaderSystemCommand(this).register();

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Found PlaceholderAPI! registering placeholders...");
            new PlaceholderHook(this).register();
        }

        this.getLogger().info("LeaderSystem Enabled.");
    }
}

package com.ericlam.mc.leadersystem.commandhandler;

import com.ericlam.mc.leadersystem.config.LangConfig;
import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.MainConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.manager.CacheManager;
import com.ericlam.mc.leadersystem.runnables.AsyncUpdateRunnable;
import com.ericlam.mc.leadersystem.runnables.ScheduleUpdateRunnable;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import com.hypernite.mc.hnmc.core.misc.commands.*;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaderSystemCommand {
    private final LeaderSystem leaderSystem;
    private final DefaultCommand root;


    public LeaderSystemCommand(LeaderSystem leaderSystem) {
        YamlManager yamlManager = leaderSystem.getYamlManager();
        CacheManager cacheManager = leaderSystem.getCacheManager();
        LeadersConfig leadersConfig = yamlManager.getConfigAs(LeadersConfig.class);
        LangConfig msg = yamlManager.getConfigAs(LangConfig.class);
        MainConfig config = yamlManager.getConfigAs(MainConfig.class);
        this.leaderSystem = leaderSystem;
        CommandNode update = new CommandNodeBuilder("update").description("強制更新排行戰績").placeholder("[stat]").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    var stat = list.size() > 0 ? list.get(0) : null;
                    if (stat != null) {
                        new AsyncUpdateRunnable(leaderSystem, stat).runTaskAsynchronously(leaderSystem);
                    } else {
                        new ScheduleUpdateRunnable(leaderSystem).runTask(leaderSystem);
                    }
                    commandSender.sendMessage(msg.get("force-updated"));
                    return true;
                }).build();

        CommandNode reload = new CommandNodeBuilder("reload").description("重載 yml").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    leadersConfig.reload();
                    commandSender.sendMessage(ChatColor.GREEN + "重載成功。");
                    return true;
                }).build();

        CommandNode get = new CommandNodeBuilder("get").description("獲得自己/別人戰績的排行與數值").permission(Perm.ADMIN)
                .placeholder("<stats> [player]")
                .execute((commandSender, list) -> {
                    var item = list.get(0);
                    LeadersConfig.LeaderBoard leaderBoard = leadersConfig.stats.get(item);
                    if (leaderBoard == null) {
                        commandSender.sendMessage(msg.get("no-statistic"));
                        return true;
                    }
                    var boardsList = cacheManager.getLeaderBoard(item);
                    if (list.size() < 2) {
                        if (!(commandSender instanceof Player)) {
                            commandSender.sendMessage("not player");
                            return true;
                        }
                        Player player = (Player) commandSender;
                        Utils.getBoard(boardsList, player.getUniqueId()).ifPresentOrElse(board ->
                                        player.sendMessage(msg.get("get-statistic").replaceAll("<item>", list.get(0))
                                                .replaceAll("<rank>", board.getRank() + "")
                                                .replaceAll("<data>", board.getDataShow())),
                                () -> player.sendMessage(msg.get("not-in-limit").replace("<limit>", config.selectLimit + "")));
                    } else {
                        String target = list.get(1);
                        Utils.getBoard(boardsList, target).ifPresentOrElse(board ->
                                        commandSender.sendMessage(msg.get("get-statistic-player").replaceAll("<player>", target)
                                                .replaceAll("<item>", list.get(0))
                                                .replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow())),
                                () -> commandSender.sendMessage(msg.get("not-in-limit").replace("<limit>", config.selectLimit + "")));
                    }
                    return true;
                }).build();

        CommandNode inv = new AdvCommandNodeBuilder<Player>("inv").description("打開該戰績的排行界面").alias("openinv", "gui").placeholder("<stats>")
                .execute((player, list) -> {
                    var item = list.get(0);
                    var inventory = cacheManager.getLeaderInventory(item);
                    if (inventory == null) {
                        player.sendMessage(msg.get("no-statistic"));
                        return true;
                    }
                    player.openInventory(inventory);
                    return true;
                }).build();
        this.root = new DefaultCommandBuilder("leadersystem").description("LeaderSystem 主指令").children(update, get, inv, reload).build();
    }


    public void register() {
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(leaderSystem, this.root);
    }
}

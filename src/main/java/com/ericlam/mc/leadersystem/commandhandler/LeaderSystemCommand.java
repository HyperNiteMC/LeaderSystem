package com.ericlam.mc.leadersystem.commandhandler;

import com.ericlam.mc.leadersystem.config.LangConfig;
import com.ericlam.mc.leadersystem.config.LeadersConfig;
import com.ericlam.mc.leadersystem.config.MainConfig;
import com.ericlam.mc.leadersystem.main.LeaderSystem;
import com.ericlam.mc.leadersystem.main.Utils;
import com.ericlam.mc.leadersystem.runnables.DataUpdateRunnable;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import com.hypernite.mc.hnmc.core.misc.commands.*;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;

public class LeaderSystemCommand {
    private LeaderSystem leaderSystem;
    private DefaultCommand root;


    public LeaderSystemCommand(LeaderSystem leaderSystem) {
        YamlManager manager = LeaderSystem.getYamlManager();
        LeadersConfig leadersConfig = manager.getConfigAs(LeadersConfig.class);
        LangConfig msg = manager.getConfigAs(LangConfig.class);
        MainConfig config = manager.getConfigAs(MainConfig.class);
        this.leaderSystem = leaderSystem;
        CommandNode update = new CommandNodeBuilder("update").description("強制更新排行戰績").permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    new DataUpdateRunnable(leaderSystem).runTask(leaderSystem);
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
                    Optional<LeadersConfig.LeaderBoard> leaderBoardOptional = Utils.getItem(list.get(0));
                    if (leaderBoardOptional.isEmpty()) {
                        commandSender.sendMessage(msg.get("no-statistic"));
                        return true;
                    }
                    if (list.size() < 2) {
                        if (!(commandSender instanceof Player)) {
                            commandSender.sendMessage("not player");
                            return true;
                        }
                        Player player = (Player) commandSender;
                        LeaderSystem.getLeaderBoardManager().getRanking(list.get(0)).whenComplete((boardsList, ex) -> {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            Utils.getBoard(boardsList, player.getUniqueId()).ifPresentOrElse(board ->
                                            player.sendMessage(msg.get("get-statistic").replaceAll("<item>", list.get(0))
                                                    .replaceAll("<rank>", board.getRank() + "")
                                                    .replaceAll("<data>", board.getDataShow())),
                                    () -> player.sendMessage(msg.get("not-in-limit").replace("<limit>", config.selectLimit + "")));
                        });
                    } else {
                        String target = list.get(1);
                        LeaderSystem.getLeaderBoardManager().getRanking(list.get(0)).whenComplete((boardsList, ex) -> {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            Utils.getBoard(boardsList, target).ifPresentOrElse(board ->
                                            commandSender.sendMessage(msg.get("get-statistic-player").replaceAll("<player>", target)
                                                    .replaceAll("<item>", list.get(0))
                                                    .replaceAll("<rank>", board.getRank() + "").replaceAll("<data>", board.getDataShow())),
                                    () -> commandSender.sendMessage(msg.get("not-in-limit").replace("<limit>", config.selectLimit + "")));
                        });
                    }
                    return true;
                }).build();

        CommandNode inv = new AdvCommandNodeBuilder<Player>("inv").description("打開該戰績的排行界面").alias("openinv", "gui").placeholder("<stats>")
                .execute((player, list) -> {
                    Utils.getItem(list.get(0)).ifPresentOrElse(leaderBoard -> {
                        LeaderSystem.getLeaderInventoryManager().getLeaderInventory(list.get(0), leaderBoard).whenComplete((inventory, ex) -> {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            Bukkit.getScheduler().runTask(leaderSystem, () -> player.openInventory(inventory));
                        });
                    }, () -> player.sendMessage(msg.get("no-statistic")));
                    return true;
                }).build();
        this.root = new DefaultCommandBuilder("leadersystem").description("LeaderSystem 主指令").children(update, get, inv, reload).build();
    }


    public void register() {
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(leaderSystem, this.root);
    }
}

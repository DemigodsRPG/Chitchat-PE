package com.demigodsrpg.chitchat.command;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import com.demigodsrpg.chitchat.ChitchatPlugin;
import net.md_5.bungee.api.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CCMuteListCommand implements CommandExecutor {

    private final ChitchatPlugin INST;

    public CCMuteListCommand(ChitchatPlugin inst) {
        INST = inst;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("ccmutelist")) {
            if (!INST.getMuteMap().isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "// -- Currently Muted Players -- //");
                for (Map.Entry<String, Double> entry : INST.getMuteMap().entrySet()) {
                    String mutedName = Server.getInstance().getOfflinePlayer(entry.getKey()).getName();
                    String mutedDate = prettyDate(entry.getValue().longValue());
                    sender.sendMessage(ChatColor.YELLOW + "  - " + ChatColor.WHITE + (mutedName != null ?
                            mutedName : entry.getKey()) + mutedDate);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "There are currently no muted players.");
            }
        }
        return true;
    }

    private String prettyDate(long time) {
        long diff = (time - System.currentTimeMillis()) / 1_000;
        int dayDiff = Math.round(diff / 86_400);

        String ret = ChatColor.YELLOW + " for " + ChatColor.WHITE;

        if (dayDiff == 0 && diff < 50) {
            ret += "less than a minute.";
        } else if (diff < 60) {
            ret += "a minute.";
        } else if (diff < 120) {
            ret += "a couple minutes.";
        } else if (diff < 3_300) {
            ret += "~" + Math.round(diff / 60) + " minutes.";
        } else if (diff < 7_200) {
            ret += "an hour.";
        } else if (diff < 82_800) {
            ret += Math.round(diff / 3_600) + " hours.";
        } else if (dayDiff <= 1) {
            ret += "a day.";
        } else if (dayDiff < 7) {
            ret += "~" + dayDiff + " days.";
        } else if (dayDiff < 28) {
            ret += "~" + (int) Math.ceil(dayDiff / 7) + " weeks.";
        } else {
            ret = ChatColor.YELLOW + " until " + ChatColor.WHITE + new SimpleDateFormat("EEE, MMM dd, yyyy.").
                    format(new Date(time));
        }

        return ret;
    }
}

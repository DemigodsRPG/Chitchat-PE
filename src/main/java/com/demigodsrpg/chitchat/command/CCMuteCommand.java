package com.demigodsrpg.chitchat.command;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CCMuteCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length > 0) {
                if (command.getName().equals("ccmute")) {
                    if ("list".equalsIgnoreCase(args[0])) {
                        sender.sendMessage(ChatColor.YELLOW + "// -- Currently Muted Players -- //");
                        for (String mutedName : Chitchat.getMuteSet()) {
                            sender.sendMessage(ChatColor.YELLOW + "  - " + mutedName);
                        }
                        return true;
                    }
                    Chitchat.getMuteSet().add(args[0]);
                    sender.sendMessage(ChatColor.YELLOW + "Muted " + args[0]);
                } else {
                    Chitchat.getMuteSet().remove(args[0]);
                    sender.sendMessage(ChatColor.YELLOW + "Unmuted " + args[0]);
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> guess = new ArrayList<>();
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length == 1) {
                if (command.getName().equals("ccunmute")) {
                    for (String muted : Chitchat.getMuteSet()) {
                        if (muted.toLowerCase().startsWith(args[0].toLowerCase())) {
                            guess.add(muted);
                        }
                    }
                } else {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            guess.add(online.getName());
                        }
                    }
                }
            }
        }
        return guess;
    }
}

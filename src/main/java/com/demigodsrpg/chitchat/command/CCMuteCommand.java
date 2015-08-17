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
import java.util.UUID;

public class CCMuteCommand implements TabExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length > 0) {
                if (command.getName().equals("ccmute")) {
                    if ("list".equalsIgnoreCase(args[0])) {
                        sender.sendMessage(ChatColor.YELLOW + "// -- Currently Muted Players -- //");
                        for (String mutedId : Chitchat.getMuteSet()) {
                            String mutedName = Bukkit.getOfflinePlayer(UUID.fromString(mutedId)).getName();
                            sender.sendMessage(ChatColor.YELLOW + "  - " + (mutedName != null ? mutedName : mutedId));
                        }
                        return true;
                    }
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(Chitchat.getInst(), new Runnable() {
                        @Override
                        public void run() {
                            String mutedId = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
                            try {
                                Chitchat.getMuteSet().add(mutedId);
                                sender.sendMessage(ChatColor.YELLOW + "Muted " + args[0]);
                            } catch (Exception oops) {
                                sender.sendMessage(ChatColor.RED + args[0] + " does not exist, try again.");
                            }
                        }
                    });
                } else {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(Chitchat.getInst(), new Runnable() {
                        @Override
                        public void run() {
                            String mutedId = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
                            try {
                                Chitchat.getMuteSet().remove(mutedId);
                                sender.sendMessage(ChatColor.YELLOW + "Unmuted " + args[0]);
                            } catch (Exception oops) {
                                sender.sendMessage(ChatColor.RED + args[0] + " does not exist, try again.");
                            }
                        }
                    });
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

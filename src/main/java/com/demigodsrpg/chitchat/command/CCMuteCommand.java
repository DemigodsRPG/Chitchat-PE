package com.demigodsrpg.chitchat.command;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CCMuteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length > 0) {
                if (command.getName().equals("ccmute")) {
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
}

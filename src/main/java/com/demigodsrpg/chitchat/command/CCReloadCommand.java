package com.demigodsrpg.chitchat.command;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CCReloadCommand implements CommandExecutor {
    private final Chitchat INST;

    public CCReloadCommand(Chitchat inst) {
        INST = inst;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("chitchat.reload")) {
            Bukkit.getServer().getPluginManager().disablePlugin(INST);
            Bukkit.getServer().getPluginManager().enablePlugin(INST);
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
        }
        return true;
    }
}

package com.demigodsrpg.chitchat.command;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import com.demigodsrpg.chitchat.ChitchatPlugin;
import net.md_5.bungee.api.ChatColor;

public class CCReloadCommand implements CommandExecutor {
    private final ChitchatPlugin INST;

    public CCReloadCommand(ChitchatPlugin inst) {
        INST = inst;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("chitchat.reload")) {
            Server.getInstance().getPluginManager().disablePlugin(INST);
            Server.getInstance().getPluginManager().enablePlugin(INST);
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
        }
        return true;
    }
}

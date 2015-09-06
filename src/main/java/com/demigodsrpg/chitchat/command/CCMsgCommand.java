package com.demigodsrpg.chitchat.command;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchat.PrivateMessage;
import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CCMsgCommand implements CommandExecutor {
    private final Chitchat INST;

    public CCMsgCommand(Chitchat inst) {
        INST = inst;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("chitchat.msg") && !INST.getMuteMap().keySet().contains(sender.getName())) {
            String receiver;
            String message = Joiner.on(" ").join(args);
            if ("ccmsg".equals(command.getName()) && args.length > 1) {
                receiver = args[0];
                message = message.substring(receiver.length() + 1);
            } else if ("ccreply".equals(command.getName()) && args.length > 0) {
                String lastSent = getLastSentMsgMatch(sender);
                if (!"".equals(lastSent)) {
                    receiver = lastSent;
                } else {
                    sender.sendMessage(ChatColor.RED + "This is not a reply, please use /msg instead.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You've made a mistake with the syntax");
                return false;
            }

            if (receiver.equals(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "Why are you sending messages to yourself?");
                return true;
            }

            // Create the private message
            PrivateMessage privateMessage = new PrivateMessage(INST, receiver, sender.getName(), message);

            // Send the message
            privateMessage.send();

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
            return true;
        }
    }

    // -- PRIVATE HELPER METHODS -- //

    private String getLastSentMsgMatch(CommandSender sender) {
        if (INST.getReplyMap().containsKey(sender.getName())) {
            return INST.getReplyMap().get(sender.getName());
        }
        return "";
    }
}

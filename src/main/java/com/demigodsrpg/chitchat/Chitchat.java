/*
 * This file is part of Chitchat, licensed under the MIT License (MIT).
 *
 * Copyright (c) DemigodsRPG.com <http://www.demigodsrpg.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.demigodsrpg.chitchat;

import com.demigodsrpg.chitchat.format.ChatFormat;
import com.demigodsrpg.chitchat.tag.DefaultPlayerTag;
import com.demigodsrpg.chitchat.tag.SpecificPlayerTag;
import com.demigodsrpg.chitchat.tag.WorldPlayerTag;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The simplest plugin for chitchat.
 */
public class Chitchat extends JavaPlugin implements Listener, CommandExecutor {
    // -- STATIC OBJECTS -- //

    private static Chitchat INST;
    private static ChatFormat FORMAT;

    // -- IMPORTANT CONFIG VALUES -- //

    private String SERVER_CHANNEL;
    private boolean USE_BUNGEE;

    // -- BUKKIT ENABLE/DISABLE -- //

    @Override
    public void onEnable() {
        // Define static instance
        INST = this;
        FORMAT = new ChatFormat();

        // Handle config
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Get the server's chat channel
        SERVER_CHANNEL = getConfig().getString("bungee_channel", "default");

        // Will we use bungee?
        USE_BUNGEE = getConfig().getBoolean("use_bungee", true);

        // Default tags
        if(getConfig().getBoolean("use_examples", true)) {
            FORMAT.add(new WorldPlayerTag())
                    .add(new DefaultPlayerTag("prefix", "chitchat.admin", ChatColor.DARK_RED + "[A]", 3))
                    .add(new SpecificPlayerTag("nablu", "Nablu", ChatColor.DARK_RED + "[N]", 3))
                    .add(new SpecificPlayerTag("hqm", "HmmmQuestionMark", ChatColor.DARK_GRAY + "[DEV]", 3));
        }

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register commands
        getCommand("ccreload").setExecutor(this);
        getCommand("ccmute").setExecutor(this);
        getCommand("ccunmute").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Manually unregister events
        HandlerList.unregisterAll((Listener) this);
    }

    // -- API METHODS -- //

    /**
     * Get the chat format for adding tags or changing other settings.
     *
     * @return The enabled chat format.
     */
    public static ChatFormat getChatFormat() {
        return FORMAT;
    }

    /**
     * Set the entire chat format to a custom version.
     *
     * @param chatFormat A custom chat format.
     * @deprecated Only use this if you know what you are doing.
     */
    @Deprecated
    public static void setChatFormat(ChatFormat chatFormat) {
        FORMAT = chatFormat;
    }

    /**
     * Get the instance of this plugin.
     *
     * @return The current instance of this plugin.
     */
    public static Chitchat getInst() {
        return INST;
    }

    // -- BUKKIT CHAT LISTENER -- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent chat) {
        chat.setFormat(FORMAT.getFormattedMessage(chat.getPlayer(), chat.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalChat(AsyncPlayerChatEvent chat) {
        if (USE_BUNGEE && !FORMAT.shouldCancelBungee(chat)) {
            String channelRaw = "chitchat$" + chat.getPlayer().getUniqueId().toString() + "$" + SERVER_CHANNEL;
            sendBungeeMessage(chat.getPlayer(), "Forward", "ALL", channelRaw, chat.getFormat());
        }
    }

    // -- BUKKIT COMMAND EXECUTOR -- //

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "ccreload": {
                if (sender.hasPermission("chitchat.reload")) {
                    getServer().getPluginManager().disablePlugin(this);
                    getServer().getPluginManager().enablePlugin(this);
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                    return true;
                }
            }
            case "ccmute":
            case "ccunmute": {
                if (sender.hasPermission("chitchat.mute")) {
                    if (args.length > 0) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null) {
                            String channelRaw = (command.getName().equals("ccmute") ? "chitchatmute$" : "chitchatunmute$")
                                    + target.getUniqueId().toString() + "$" + SERVER_CHANNEL;
                            sendBungeeMessage(target, "Forward", "ALL", channelRaw, "");
                        } else {
                            sender.sendMessage(ChatColor.RED + "That player is not currently on this server.");
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                    return true;
                }
            }
            default: {
                return false;
            }
        }
    }

    // -- PRIVATE HELPER METHODS -- //

    private void sendBungeeMessage(Player target, String messageType, String targetServer, String channel, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(messageType);
        out.writeUTF(targetServer);
        out.writeUTF(channel);

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        try {
            msgout.writeUTF(message);
        } catch (IOException ignored) {
        }

        // Write the message
        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        target.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }
}

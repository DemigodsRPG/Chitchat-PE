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
import org.redisson.Config;
import org.redisson.Redisson;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * The simplest plugin for chitchat.
 */
public class Chitchat extends JavaPlugin implements Listener, CommandExecutor {
    // -- STATIC OBJECTS -- //

    private static Chitchat INST;
    private static ChatFormat FORMAT;

    // -- REDIS OBJECTS -- //
    private boolean USE_REDIS;
    private String SERVER_CHANNEL;
    private String SERVER_ID;

    private Redisson REDIS;

    // -- REDIS DATA -- //'

    private Queue<String> CHAT_QUEUE;
    private Set<String> MUTE_LIST;

    // -- BUKKIT ENABLE/DISABLE -- //

    @Override
    public void onEnable() {
        // Define static instance
        INST = this;
        FORMAT = new ChatFormat();

        // Handle config
        getConfig().options().copyDefaults(true);
        saveConfig();

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

        // Will we use redis?
        USE_REDIS = getConfig().getBoolean("redis.use", true);

        // Redis stuff
        if (USE_REDIS) {
            // Get the server's id and chat channel
            SERVER_ID = getConfig().getString("redis.server_id", "minecraft");
            SERVER_CHANNEL = getConfig().getString("redis.channel", "default");

            // Configure and connect to redis
            Config config = new Config();
            config.useSingleServer().setAddress(getConfig().getString("redis.connection", "127.0.0.1:6379"));
            REDIS = Redisson.create(config);

            // Setup chat queue
            CHAT_QUEUE = REDIS.getQueue(SERVER_CHANNEL + "\\$" + "chat");

            // Setup mute list
            MUTE_LIST = REDIS.getSet(SERVER_CHANNEL + "\\$" + "mute");

            // Start redis listen task
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new RedisListenTask(), 20, 1);

            // Make sure everything connected, if not, disable the plugin
            if (CHAT_QUEUE != null) {
                getLogger().info("Redis connection was successful.");
            } else {
                getLogger().severe("Redis connection was unsuccessful!");
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            // Setup mute list
            MUTE_LIST = new HashSet<>();
        }
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
        if (MUTE_LIST.contains(chat.getPlayer().getName())) {
            chat.setCancelled(true);
            return;
        }
        chat.setFormat(FORMAT.getFormattedMessage(chat.getPlayer(), chat.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFinalChat(AsyncPlayerChatEvent chat) {
        if (USE_REDIS && !FORMAT.shouldCancelRedis(chat)) {
            CHAT_QUEUE.add(SERVER_ID + "\\$" + chat.getFormat());
        }
    }

    // -- REDIS LISTEN TASK -- //

    public class RedisListenTask implements Runnable {
        private String lastMessage = "";
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            String message = CHAT_QUEUE.peek();
            if (lastMessage.equals(message)) {
                if (lastTime <= System.currentTimeMillis() - 200) {
                    CHAT_QUEUE.remove(lastMessage);
                }
            } else if (message != null && !message.startsWith(SERVER_ID + "\\$")) {
                Bukkit.broadcastMessage(message.substring(message.indexOf('$') + 1));
                lastMessage = message;
                lastTime = System.currentTimeMillis();
            }
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
                if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
                    if (args.length > 0) {
                        if (command.getName().equals("ccmute")) {
                            MUTE_LIST.add(args[0]);
                            sender.sendMessage(ChatColor.YELLOW + "Muted " + args[0]);
                        } else {
                            MUTE_LIST.remove(args[0]);
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
            default: {
                return false;
            }
        }
    }
}

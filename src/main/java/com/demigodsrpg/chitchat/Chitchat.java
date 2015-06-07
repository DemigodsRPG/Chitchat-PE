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

import com.demigodsrpg.chitchat.command.CCMsgCommand;
import com.demigodsrpg.chitchat.command.CCMuteCommand;
import com.demigodsrpg.chitchat.command.CCReloadCommand;
import com.demigodsrpg.chitchat.format.ChatFormat;
import com.demigodsrpg.chitchat.tag.DefaultPlayerTag;
import com.demigodsrpg.chitchat.tag.SpecificPlayerTag;
import com.demigodsrpg.chitchat.tag.WorldPlayerTag;
import com.demigodsrpg.chitchat.util.LibraryHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The simplest plugin for chitchat.
 */
public class Chitchat extends JavaPlugin implements Listener {
    // -- STATIC OBJECTS -- //

    private static Chitchat INST;
    private static TitleUtil TITLE;
    static ChatFormat FORMAT;
    static LibraryHandler LIBRARIES;

    // -- IMPORTANT LISTS -- //
    Set<String> MUTE_SET;
    Map<String, String> REPLY_MAP;

    // -- OPTIONS -- //

    boolean OVERRIDE_ME;
    boolean USE_REDIS;

    // -- BUKKIT ENABLE/DISABLE -- //

    @Override
    public void onEnable() {
        // Define static objects
        INST = this;
        FORMAT = new ChatFormat();
        LIBRARIES = new LibraryHandler(this);

        // Handle config
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Override /me
        OVERRIDE_ME = getConfig().getBoolean("override_me", true);

        // Default tags
        if(getConfig().getBoolean("use_examples", true)) {
            FORMAT.add(new WorldPlayerTag())
                    .add(new DefaultPlayerTag("prefix", "chitchat.admin", ChatColor.DARK_RED + "[A]", 3))
                    .add(new SpecificPlayerTag("hqm", "HmmmQuestionMark", ChatColor.DARK_GRAY + "[DEV]", 3))
                    .add(new SpecificPlayerTag("hqm2", "HQM", ChatColor.DARK_GRAY + "[DEV]", 3));
        }

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        CCReloadCommand reloadCommand = new CCReloadCommand();
        CCMuteCommand muteCommand = new CCMuteCommand();
        CCMsgCommand msgCommand = new CCMsgCommand();
        getCommand("ccreload").setExecutor(reloadCommand);
        getCommand("ccmute").setExecutor(muteCommand);
        getCommand("ccunmute").setExecutor(muteCommand);
        getCommand("ccmsg").setExecutor(msgCommand);
        getCommand("ccreply").setExecutor(msgCommand);

        // Will we use redis?
        USE_REDIS = getConfig().getBoolean("redis.use", true);

        // Redis stuff
        if (USE_REDIS) {
            // Add the required libraries
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.ORG_REDISSON,
                    Depends.REDISSON, Depends.REDISSON_VER);
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.ORG_SLF4J,
                    Depends.SLF4J_API, Depends.SLF4J_API_VER);
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.COM_ESOTERICSOFTWARE,
                    Depends.KYRO, Depends.KYRO_VER);
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.COM_FASTERXML_JACKSON_CORE,
                    Depends.JACKSON_CORE, Depends.JACKSON_VER);
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.COM_FASTERXML_JACKSON_CORE,
                    Depends.JACKSON_ANNOTATIONS, Depends.JACKSON_VER);
            LIBRARIES.addMavenLibrary(LibraryHandler.MAVEN_CENTRAL, Depends.COM_FASTERXML_JACKSON_CORE,
                    Depends.JACKSON_DATABIND, Depends.JACKSON_VER);

            // Setup redis related stuff
            getServer().getPluginManager().registerEvents(new RChitchat(this), this);
        }

        if (!USE_REDIS) {
            // Setup mute list
            MUTE_SET = new HashSet<>();

            // Setup private message map
            REPLY_MAP = new HashMap<>();
        }

        // FIXME DEBUG
        try {
            TITLE = new TitleUtil();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Manually unregister events
        HandlerList.unregisterAll((Plugin) this);
    }

    // -- INST API METHODS -- //

    /**
     * Get the set of all muted players.
     *
     * @return The set of all muted players.
     */
    public static Set<String> getMuteSet() {
        return getInst().MUTE_SET;
    }

    /**
     * Get a map of all recent reply pairs.
     *
     * @return Map of all recent reply pairs.
     */
    public static Map<String, String> getReplyMap() {
        return getInst().REPLY_MAP;
    }

    // -- STATIC API METHODS -- //

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
        if (MUTE_SET.contains(chat.getPlayer().getName())) {
            chat.setCancelled(true);
            return;
        }
        chat.setFormat(FORMAT.getFormattedMessage(chat.getPlayer(), chat.getMessage()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMeCommand(PlayerCommandPreprocessEvent command) {
        if (OVERRIDE_ME) {
            Player player = command.getPlayer();
            String message = command.getMessage().substring(1);

            // -- /me -- //
            if (message.startsWith("me ")) {
                command.setCancelled(true);
                if (!MUTE_SET.contains(player.getName())) {
                    message = ChatColor.ITALIC + ChatColor.stripColor(player.getDisplayName() + " " + message.substring(3));
                    if (USE_REDIS && !FORMAT.shouldCancelRedis(player)) {
                        RChitchat.REDIS_CHAT.publish(RChitchat.getServerId() + "$" + message);
                    }
                    Bukkit.broadcastMessage(message);
                }
            }
        }
    }

    // FIXME DEBUG
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        TITLE.sendTitle(event.getPlayer(), 20, 120, 20, "TEST", "This is a test.");
        event.getPlayer().sendMessage("TEST.");
    }
}

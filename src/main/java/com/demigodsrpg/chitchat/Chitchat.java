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
import com.demigodsrpg.chitchat.tag.ChatScope;
import com.demigodsrpg.chitchat.tag.DefaultPlayerTag;
import com.demigodsrpg.chitchat.tag.SpecificPlayerTag;
import com.demigodsrpg.chitchat.tag.WorldPlayerTag;
import com.demigodsrpg.chitchat.util.LibraryHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

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
    List<String> MUTED_COMMANDS;

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

        // Muted commands
        MUTED_COMMANDS = getConfig().getStringList("muted-commands");

        // Default tags
        if(getConfig().getBoolean("use_examples", true)) {
            TextComponent admin = new TextComponent("[A]");
            admin.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
            admin.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Administrator").
                    color(net.md_5.bungee.api.ChatColor.DARK_RED).create()));
            TextComponent dev = new TextComponent("[D]");
            dev.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
            dev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Developer").
                    color(net.md_5.bungee.api.ChatColor.DARK_GRAY).create()));
            FORMAT.add(new WorldPlayerTag())
                    .add(new DefaultPlayerTag("example-prefix", "chitchat.admin", admin, 3))
                    .add(new SpecificPlayerTag("hqm", "HmmmQuestionMark", dev, 3))
                    .add(new SpecificPlayerTag("hqm2", "HandyQuestMarker", dev, 3))
                    .add(new SpecificPlayerTag("hqm3", "HQM", dev, 3));
        }

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        CCReloadCommand reloadCommand = new CCReloadCommand();
        CCMuteCommand muteCommand = new CCMuteCommand();
        CCMsgCommand msgCommand = new CCMsgCommand();
        getCommand("ccreload").setExecutor(reloadCommand);
        getCommand("ccmute").setExecutor(muteCommand);
        getCommand("ccmute").setTabCompleter(muteCommand);
        getCommand("ccunmute").setExecutor(muteCommand);
        getCommand("ccunmute").setTabCompleter(muteCommand);
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
            new RChitchat(this);
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

    /**
     * Send a title message to a player.
     *
     * @param player       The player receiving the message.
     * @param fadeInTicks  The ticks the message takes to fade in.
     * @param stayTicks    The ticks the message stays on screen (sans fades).
     * @param fadeOutTicks The ticks the message takes to fade out.
     * @param title        The title text.
     * @param subtitle     The subtitle text.
     */
    public static void sendTitle(Player player, int fadeInTicks, int stayTicks, int fadeOutTicks, String title, String subtitle) {
        TITLE.sendTitle(player, fadeInTicks, stayTicks, fadeOutTicks, title, subtitle);
    }

    /**
     * Clear or reset the title data for a specified player.
     *
     * @param player The player being cleared/reset.
     * @param reset  True if reset, false for clear.
     */
    public static void clearTitle(final Player player, boolean reset) {
        TITLE.clearTitle(player, reset);
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     */
    public static void sendMessage(BaseComponent message) {
        if (getInst().USE_REDIS) {
            RChitchat.REDIS_CHAT.publish(RChitchat.getServerId() + "$" + message.toLegacyText());
        }
        Bukkit.getServer().spigot().broadcast(message);
    }

    /**
     * Send a message through the Chitchat plugin, exclusive to a list of recipients.
     *
     * @param message    The message to be sent.
     * @param recipients The recipients of this message.
     */
    public static void sendMessage(BaseComponent message, Set<Player> recipients) {
        for (Player player : recipients) {
            player.spigot().sendMessage(message);
        }
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     * @deprecated This method is depreciated in favor of the
     */
    @Deprecated
    public static void sendMessage(String message) {
        sendMessage(new TextComponent(TextComponent.fromLegacyText(message)));
    }

    // -- BUKKIT CHAT LISTENER -- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent chat) {
        if (MUTE_SET.contains(chat.getPlayer().getName())) {
            chat.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFinalChat(AsyncPlayerChatEvent chat) {
        sendMessage(FORMAT.getFormattedMessage(chat.getPlayer(), ChatScope.LOCAL, chat.getMessage()), chat.getRecipients());
        if (USE_REDIS && !FORMAT.shouldCancelRedis(chat.getPlayer())) {
            RChitchat.REDIS_CHAT.publish(RChitchat.getServerId() + "$" +
                    getChatFormat().getFormattedMessage(chat.getPlayer(), ChatScope.CHANNEL, chat.getMessage()).toLegacyText());
        }
        chat.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreprocessCommand(PlayerCommandPreprocessEvent command) {
        Player player = command.getPlayer();
        String[] commandMsg = command.getMessage().split("\\s+");

        // Muted commands
        if (MUTE_SET.contains(player.getName())) {
            if (MUTED_COMMANDS.contains(commandMsg[0].toLowerCase().substring(1))) {
                command.setCancelled(true);
                player.sendMessage(ChatColor.RED + "I'm sorry " + player.getName() + ", I'm afraid I can't do that.");
            }
        }

        // /me <message>
        else if (OVERRIDE_ME && commandMsg.length > 1 && commandMsg[0].equals("/me")) {
            command.setCancelled(true);
            if (MUTED_COMMANDS.contains("me") && MUTE_SET.contains(player.getName())) {
                player.sendMessage(ChatColor.RED + "I'm sorry " + player.getName() + ", I'm afraid I can't do that.");
            } else {
                String message = command.getMessage().substring(1);
                message = ChatColor.ITALIC + ChatColor.stripColor(player.getDisplayName() + " " + message.substring(3));
                sendMessage(new TextComponent(message));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        // Remove all useless reply data
        String playerName = event.getPlayer().getName();
        REPLY_MAP.remove(playerName);
        if (REPLY_MAP.containsValue(playerName)) {
            for (Map.Entry<String, String> entry : REPLY_MAP.entrySet()) {
                if (entry.getValue().equals(playerName)) {
                    REPLY_MAP.remove(entry.getKey());
                }
            }
        }
    }
}

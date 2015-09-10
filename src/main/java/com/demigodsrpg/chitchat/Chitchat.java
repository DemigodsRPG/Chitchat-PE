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
import com.demigodsrpg.chitchat.command.CCMuteListCommand;
import com.demigodsrpg.chitchat.command.CCReloadCommand;
import com.demigodsrpg.chitchat.format.ChatFormat;
import com.demigodsrpg.chitchat.tag.DefaultPlayerTag;
import com.demigodsrpg.chitchat.tag.SpecificPlayerTag;
import com.demigodsrpg.chitchat.tag.WorldPlayerTag;
import com.demigodsrpg.chitchat.util.JsonFileUtil;
import com.demigodsrpg.chitchat.util.LibraryHandler;
import com.demigodsrpg.chitchat.util.TitleUtil;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The simplest plugin for chitchat.
 */
public class Chitchat extends JavaPlugin {

    // -- IMPORTANT OBJECTS -- //

    static Chitchat INST;
    ChatFormat FORMAT;
    JsonFileUtil JSON;
    TitleUtil TITLE;
    LibraryHandler LIBRARIES;


    // -- IMPORTANT LISTS -- //
    ConcurrentMap<String, Double> MUTE_MAP;
    ConcurrentMap<String, String> REPLY_MAP;

    // -- OPTIONS -- //

    boolean OVERRIDE_ME;
    boolean USE_REDIS;
    boolean SAVE_MUTES;
    List<String> MUTED_COMMANDS;

    // -- BUKKIT ENABLE/DISABLE -- //

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        // Define static objects
        INST = this;
        FORMAT = new ChatFormat();
        LIBRARIES = new LibraryHandler(this);

        // Handle local data saves
        JSON = new JsonFileUtil(getDataFolder(), true);

        // Setup TitleUtil
        try {
            TITLE = new TitleUtil();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                NoSuchFieldException e) {
            e.printStackTrace();
        }

        // Handle config
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Override /me
        OVERRIDE_ME = getConfig().getBoolean("override_me", true);

        // Muted commands
        MUTED_COMMANDS = ImmutableList.copyOf(getConfig().getStringList("muted-commands"));

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
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register commands
        CCReloadCommand reloadCommand = new CCReloadCommand(this);
        CCMuteListCommand muteListCommand = new CCMuteListCommand(this);
        CCMuteCommand muteCommand = new CCMuteCommand(this, JSON);
        CCMsgCommand msgCommand = new CCMsgCommand(this);
        getCommand("ccreload").setExecutor(reloadCommand);
        getCommand("ccmutelist").setExecutor(muteListCommand);
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
            MUTE_MAP = new ConcurrentHashMap<>();

            // Setup private message map
            REPLY_MAP = new ConcurrentHashMap<>();
        }

        // Handle mute settings
        SAVE_MUTES = getConfig().getBoolean("save_mutes", false);
        if (savingMutes()) {
            try {
                MUTE_MAP = new ConcurrentHashMap<>(JSON.loadFromFile("mutes"));
            } catch (Exception oops) {
                getLogger().severe("Unable to load saved mutes, did someone tamper with the data?");
            }
        }

        // Clean up old mutes (only one server should do this to avoid unnecessary threads
        if (!USE_REDIS || getConfig().getBoolean("redis.clean_old_mutes", false)) {
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> MUTE_MAP.entrySet().stream().
                    filter(entry -> entry.getValue() < System.currentTimeMillis()).
                    forEach((Map.Entry<String, Double> entry) -> MUTE_MAP.remove(entry.getKey())), 30, 30);
        }
    }

    @Override
    public void onDisable() {
        // Manually unregister events
        HandlerList.unregisterAll(this);

        // Save mutes
        if (savingMutes()) {
            JSON.saveToFile("mutes", MUTE_MAP);
        }
    }

    // -- INST API METHODS -- //

    /**
     * Get the map of all muted players and their mute length in milliseconds.
     *
     * @return The map of all muted players.
     */
    public Map<String, Double> getMuteMap() {
        return MUTE_MAP;
    }

    /**
     * Get a map of all recent reply pairs.
     *
     * @return Map of all recent reply pairs.
     */
    public Map<String, String> getReplyMap() {
        return REPLY_MAP;
    }

    // -- STATIC API METHODS -- //

    /**
     * Get the chat format for adding tags or changing other settings.
     *
     * @return The enabled chat format.
     */
    public static ChatFormat getChatFormat() {
        return getInst().FORMAT;
    }

    /**
     * Set the entire chat format to a custom version.
     *
     * @param chatFormat A custom chat format.
     * @deprecated Only use this if you know what you are doing.
     */
    @Deprecated
    public static void setChatFormat(ChatFormat chatFormat) {
        getInst().FORMAT = chatFormat;
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
    public static void sendTitle(Player player, int fadeInTicks, int stayTicks, int fadeOutTicks, String title,
                                 String subtitle) {
        if (getInst().TITLE != null) {
            getInst().TITLE.sendTitle(player, fadeInTicks, stayTicks, fadeOutTicks, title, subtitle);
        }
    }

    /**
     * Clear or reset the title data for a specified player.
     *
     * @param player The player being cleared/reset.
     * @param reset  True if reset, false for clear.
     */
    public static void clearTitle(final Player player, boolean reset) {
        if (getInst().TITLE != null) {
            getInst().TITLE.clearTitle(player, reset);
        }
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     */
    @SuppressWarnings("unchecked")
    public static void sendMessage(BaseComponent message) {
        if (getInst().USE_REDIS) {
            RChitchat.REDIS_CHAT.publish(RChitchat.getInst().getServerId() + "$" + message.toLegacyText());
        }
        sendMessage(message, (Collection<Player>) Bukkit.getServer().getOnlinePlayers());
    }

    /**
     * Send a message through the Chitchat plugin, exclusive to a list of recipients.
     *
     * @param message    The message to be sent.
     * @param recipients The recipients of this message.
     */
    public static void sendMessage(BaseComponent message, Collection<Player> recipients) {
        for (Player player : recipients) {
            player.spigot().sendMessage(message);
        }
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     * @deprecated This method is depreciated in favor of the new BaseComponent based method.
     */
    @Deprecated
    public static void sendMessage(String message) {
        sendMessage(new TextComponent(TextComponent.fromLegacyText(message)));
    }

    // -- OPTION GETTERS -- //

    public boolean usingRedis() {
        return getInst().USE_REDIS;
    }

    public boolean savingMutes() {
        return !getInst().USE_REDIS && getInst().SAVE_MUTES;
    }
}

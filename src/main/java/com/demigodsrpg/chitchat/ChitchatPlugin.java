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

import cn.nukkit.event.HandlerList;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.demigodsrpg.chitchat.format.ChatFormat;
import com.demigodsrpg.chitchat.util.JsonFileUtil;
import com.demigodsrpg.chitchat.util.LibraryHandler;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The simplest plugin for chitchat.
 */
public class ChitchatPlugin extends PluginBase {

    // -- IMPORTANT OBJECTS -- //

    static ChitchatPlugin INST;
    ChatFormat FORMAT;
    JsonFileUtil JSON;
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
        // Library Handler & Bungeecord Chat API
        LIBRARIES = new LibraryHandler(this);
        LIBRARIES.addMavenLibrarySnapshot(Depends.OSS_SONATYPE, Depends.NET_MD_5,
                Depends.BUNGEE_CHAT, Depends.BUNGEE_CHAT_VER, Depends.BUNGEE_CHAT_SNAPSHOT);

        // Define main static objects
        INST = this;
        FORMAT = new ChatFormat();

        // Handle local data saves
        JSON = new JsonFileUtil(getDataFolder(), true);

        // Handle config
        this.saveResource("config.yml");
        Config config = new Config(new File(this.getDataFolder(), "config.yml"), Config.YAML);
        config.save();

        // Override /me
        OVERRIDE_ME = getConfig().getBoolean("override_me", true);

        // Muted commands
        MUTED_COMMANDS = ImmutableList.copyOf(getConfig().getStringList("muted-commands"));

        // Default tags
        Chitchat.useExamples(this);

        // Register events
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register commands
        /*CCReloadCommand reloadCommand = new CCReloadCommand(this);
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
        getCommand("ccreply").setExecutor(msgCommand);*/

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
        if (SAVE_MUTES) {
            try {
                MUTE_MAP = new ConcurrentHashMap<>(JSON.loadFromFile("mutes"));
            } catch (Exception oops) {
                getLogger().critical("Unable to load saved mutes, did someone tamper with the data?");
            }
        }

        // Clean up old mutes (only one server should do this to avoid unnecessary threads
        if (!USE_REDIS || getConfig().getBoolean("redis.clean_old_mutes", false)) {
            getServer().getScheduler().scheduleRepeatingTask(() -> MUTE_MAP.entrySet().stream().
                    filter(entry -> entry.getValue() < System.currentTimeMillis()).
                    forEach((Map.Entry<String, Double> entry) -> MUTE_MAP.remove(entry.getKey())), 30, true);
        }
    }

    @Override
    public void onDisable() {
        // Manually unregister events
        HandlerList.unregisterAll(this);

        // Save mutes
        if (SAVE_MUTES) {
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
        return getInst().MUTE_MAP;
    }

    /**
     * Get a map of all recent reply pairs.
     *
     * @return Map of all recent reply pairs.
     */
    public Map<String, String> getReplyMap() {
        return getInst().REPLY_MAP;
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
    public static ChitchatPlugin getInst() {
        return ChitchatPlugin.INST;
    }

    // -- OPTION GETTERS -- //

    public boolean usingRedis() {
        return getInst().USE_REDIS;
    }

    public boolean savingMutes() {
        return !getInst().USE_REDIS && getInst().SAVE_MUTES;
    }
}

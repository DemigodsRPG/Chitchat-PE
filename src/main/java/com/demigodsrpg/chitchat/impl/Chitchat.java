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
package com.demigodsrpg.chitchat.impl;

import com.demigodsrpg.chitchat.api.ChatFormat;
import com.demigodsrpg.chitchat.api.DefaultPlayerTag;
import com.demigodsrpg.chitchat.api.SpecificPlayerTag;
import com.demigodsrpg.chitchat.impl.tags.WorldPlayerTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The simplest plugin for chitchat.
 */
public class Chitchat extends JavaPlugin implements Listener {
    private static Chitchat INST;
    private static ChatFormat FORMAT;

    @Override
    public void onEnable() {
        // Define static instance
        INST = this;
        FORMAT = new ChatFormat();

        // Default tags
        FORMAT.add(new WorldPlayerTag())
        .add(new DefaultPlayerTag("prefix", "chitchat.admin", "&4[A]", 3))
        .add(new SpecificPlayerTag("nablu", "Nablu", "&4[N]", 3))
        .add(new SpecificPlayerTag("hqm", "HmmmQuestionMark", "&8[DEV]", 3));

        // Register event
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Unregister events
        HandlerList.unregisterAll((Listener) this);
    }

    public static ChatFormat getChatFormat() {
        return FORMAT;
    }

    public static void setChatFormat(ChatFormat chatFormat) {
        FORMAT = chatFormat;
    }

    public static Chitchat getInst() {
        return INST;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent chat) {
        chat.setFormat(FORMAT.getFormattedMessage(chat.getPlayer(), chat.getMessage()));
    }
}

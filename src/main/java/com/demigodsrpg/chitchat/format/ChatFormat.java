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
package com.demigodsrpg.chitchat.format;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchat.tag.PlayerTag;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * A class representing the format of chat.
 */
public class ChatFormat {
    // -- IMPORTANT DATA -- //

    private final List<PlayerTag> playerTags = new LinkedList<PlayerTag>();
    private final String format;

    // -- CONSTRUCTORS -- //

    /**
     * Create a default chat format.
     */
    public ChatFormat() {
        this(Chitchat.getInst().getConfig().getString("format", "+tags&7+displayname&7: &f+message"));
    }

    /**
     * Create a chat format from a string representing the format.
     *
     * @param format The string representing the format.
     */
    public ChatFormat(String format) {
        this.format = format;
    }

    // -- MUTATORS -- //

    /**
     * Add a player tag to the chat format.
     *
     * @param playerTag A player tag.
     * @return This chat format.
     */
    public ChatFormat add(PlayerTag playerTag) {
        // Negative priorities don't exist
        if(playerTag.getPriority() < 0) {
            playerTags.add(0, playerTag);
        }

        // Make sure the priority fits into the linked list
        else if(playerTag.getPriority() + 1 > playerTags.size()) {
            playerTags.add(playerTag);
        }

        // Add to the correct spot
        else {
            playerTags.add(playerTag.getPriority(), playerTag);
        }
        return this;
    }

    /**
     * Add a collection of player tags to the chat format.
     *
     * @param playerTags A collection of player tags.
     * @return This chat format.
     */
    public ChatFormat addAll(Collection<PlayerTag> playerTags) {
        for(PlayerTag tag : playerTags) {
            add(tag);
        }
        return this;
    }

    /**
     * Add an array of player tags to the chat format.
     *
     * @param playerTags An array of player tags.
     * @return This chat format.
     */
    public ChatFormat addAll(PlayerTag[] playerTags) {
        for(PlayerTag tag : playerTags) {
            add(tag);
        }
        return this;
    }

    // -- GETTERS -- //

    /**
     * Get an immutable copy of the player tags.
     *
     * @return An immutable copy of the player tags.
     */
    public ImmutableList<PlayerTag> getPlayerTags() {
        return ImmutableList.copyOf(playerTags);
    }

    /**
     * Get the string representation of all of the player tags.
     *
     * @param player The player for whom the tags will be applied.
     * @return The string of the final tag results.
     */
    public String getTagsString(Player player) {
        String formatted = "";
        for (PlayerTag tag : playerTags) {
            formatted += tag.getFor(player);
        }
        return formatted;
    }

    /**
     * Get the format base string.
     *
     * @return The format base string.
     */
    public String getFormatString() {
        return format;
    }

    /**
     * Get the final formatted message for this chat format.
     *
     * @param player The player chatting.
     * @param message The message being sent.
     * @return The final formatted message.
     */
    public String getFormattedMessage(Player player, String message) {
        return ChatColor.translateAlternateColorCodes('&', format.
                replace("+tags", getTagsString(player)).
                replace("+message", message).
                replace("+displayname", player.getDisplayName()));
    }
}
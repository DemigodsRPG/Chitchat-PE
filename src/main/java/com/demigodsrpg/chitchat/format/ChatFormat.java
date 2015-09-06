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

import com.demigodsrpg.chitchat.tag.ChatScope;
import com.demigodsrpg.chitchat.tag.PlayerTag;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A class representing the format of chat.
 */
public class ChatFormat {
    // -- IMPORTANT DATA -- //

    private final List<PlayerTag> playerTags = new LinkedList<>();

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
        playerTags.forEach(this::add);
        return this;
    }

    /**
     * Add an array of player tags to the chat format.
     *
     * @param playerTags An array of player tags.
     * @return This chat format.
     */
    public ChatFormat addAll(PlayerTag[] playerTags) {
        Arrays.asList(playerTags).forEach(this::add);
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
     * Get the representation of all of the player tags.
     *
     * @param player The player for whom the tags will be applied.
     * @param scope The scope for the tag to be presented in.
     * @return The tag results.
     */
    public TextComponent getTags(TextComponent parent, Player player, ChatScope scope) {
        playerTags.stream().filter(tag -> tag.getScope().equals(scope) || ChatScope.ALL.equals(tag.getScope())).
                forEach(tag -> {
                    TextComponent component = tag.getComponentFor(player);
                    if (component != null) {
                        parent.addExtra(component.duplicate());
            }
                });
        return parent;
    }

    /**
     * Get the final formatted message for this chat format.
     *
     * @param player The player chatting.
     * @param scope The scope for the message to be presented in.
     * @param message The message being sent.
     * @return The final formatted message.
     */
    public BaseComponent getFormattedMessage(Player player, ChatScope scope, String message) {
        TextComponent ret = new TextComponent("");
        ret = getTags(ret, player, scope);
        ret.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        for (BaseComponent component : TextComponent.fromLegacyText(player.getDisplayName())) {
            ret.addExtra(component);
        }
        TextComponent next = new TextComponent(": ");
        next.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
        ret.addExtra(next);
        String finalMessage = ChatColor.WHITE + message;
        if (player.hasPermission("chitchat.color")) {
            finalMessage = ChatColor.translateAlternateColorCodes('&', finalMessage);
        }
        for (BaseComponent component : TextComponent.fromLegacyText(finalMessage)) {
            ret.addExtra(component);
        }
        return ret;
    }

    /**
     * Should this message not be sent over bungee?
     *
     * @param player The player.
     * @return If the message should be sent over bungee.
     */
    public boolean shouldCancelRedis(Player player) {
        for (PlayerTag tag : getPlayerTags()) {
            if (tag.cancelRedis(player)) {
                return true;
            }
        }
        return false;
    }
}

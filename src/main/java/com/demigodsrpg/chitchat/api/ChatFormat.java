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
package com.demigodsrpg.chitchat.api;

import com.demigodsrpg.chitchat.impl.Chitchat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatFormat {
    private final List<PlayerTag> playerTags = new LinkedList<PlayerTag>();
    private final String format;

    public ChatFormat() {
        this(Chitchat.getInst().getConfig().getString("format", "+tags&7: &f+message"));
    }

    public ChatFormat(String format) {
        this.format = format;
    }

    public ChatFormat add(PlayerTag playerTag) {
        if(playerTag.getPriority() < 0) {
            playerTags.add(0, playerTag);
        } else if(playerTag.getPriority() + 1 > playerTags.size()) {
            playerTags.add(playerTag);
        } else {
            playerTags.add(playerTag.getPriority(), playerTag);
        }
        return this;
    }

    public String getFormattedTags(Player player) {
        String formatted = "";
        for(PlayerTag tag : playerTags) {
            formatted += tag.getFor(player);
        }
        return formatted;
    }

    public String getFormattedMessage(Player player, String message) {
        return ChatColor.translateAlternateColorCodes('&', format.replace("+tags", getFormattedTags(player)).replace("+message", message));
    }
}

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
package com.demigodsrpg.chitchat.tag;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * A default tag based on a player's current world.
 */
public class WorldPlayerTag extends PlayerTag {
    // -- NAME CACHE -- //

    private final Map<String, String> TEXT_CACHE = new HashMap<String, String>();

    // -- GETTERS -- //

    @Override
    public String getName() {
        return "world";
    }

    @Override
    public String getFor(Player tagSource) {
        // Get world name
        String worldName = tagSource.getWorld().getName();

        // Check the cache
        if(TEXT_CACHE.containsKey(worldName)) {
            return TEXT_CACHE.get(worldName);
        }

        // Generate the tag text
        String tagText = ChatColor.translateAlternateColorCodes('&', Chitchat.getInst().getConfig().getString("worlds." + worldName + ".text", "[" + worldName.toUpperCase() + "]"));
        TEXT_CACHE.put(worldName, tagText);
        return tagText;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}

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

import cn.nukkit.Player;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A player tag that applies to a specific player name.
 */
public class SpecificPlayerTag extends PlayerTag {
    // -- IMPORTANT DATA -- //

    private final String name;
    private final String playerName;
    private final TextComponent tagText;
    private final int priority;

    // -- CONSTRUCTOR -- //

    public SpecificPlayerTag(String name, String playerName, TextComponent tagText, int priority) {
        this.name = name;
        this.playerName = playerName;
        this.tagText = tagText;
        this.priority = priority;
    }

    // -- GETTERS -- //

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TextComponent getComponentFor(Player tagSource) {
        if(tagSource.getName().equals(playerName)) {
            return tagText;
        }
        return null;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}

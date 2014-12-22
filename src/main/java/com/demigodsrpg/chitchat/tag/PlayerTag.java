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

import org.bukkit.entity.Player;

/**
 * An interface representing a player tag.
 */
public interface PlayerTag {
    /**
     * Get the name of this player tag.
     *
     * @return The name.
     */
    String getName();

    /**
     * Get the tag result for a player.
     *
     * @param tagSource The player.
     * @return The tag result.
     */
    String getFor(Player tagSource);

    /**
     * Under certain conditions, this tag will cancel sending the chat message to bungee.
     *
     * @param tagSource The player.
     * @return The message shouldn't be sent to bungee.
     */
    boolean cancelBungee(Player tagSource);

    /**
     * Get the priority (0 being leftmost, all larger being to the right).
     *
     * @return The priority.
     */
    int getPriority();
}

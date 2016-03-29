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
 * An interface representing a player tag.
 */
public abstract class PlayerTag {
    /**
     * Get the name of this player tag.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * Get the tag result for a player.
     *
     * @param tagSource The player.
     * @return The tag result.
     */
    public TextComponent getComponentFor(Player tagSource) {
        return new TextComponent(TextComponent.fromLegacyText(getFor(tagSource)));
    }

    /**
     * Get the tag result for a player.
     *
     * @param tagSource The player.
     * @return The tag result.
     * @deprecated This method uses legacy
     */
    @Deprecated
    public String getFor(Player tagSource) {
        throw new UnsupportedOperationException("This method is not supported.");
    }

    /**
     * Get the tag's chat scope.
     *
     * @return The scope for this tag to be presented in.
     */
    public ChatScope getScope() {
        return ChatScope.ALL;
    }

    /**
     * Should this player currently be not be sending chat over redis?
     *
     * @param tagSource The player.
     * @return The message shouldn't be sent to bungee.
     */
    public boolean cancelRedis(Player tagSource) {
        return false;
    }

    /**
     * Get the priority (0 being leftmost, all larger being to the right).
     *
     * @return The priority.
     */
    public abstract int getPriority();
}

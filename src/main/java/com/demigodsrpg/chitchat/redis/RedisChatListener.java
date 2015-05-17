package com.demigodsrpg.chitchat.redis;

import com.demigodsrpg.chitchat.RChitchat;
import org.bukkit.Bukkit;
import org.redisson.core.MessageListener;

public class RedisChatListener implements MessageListener<String> {
    @Override
    public void onMessage(String message) {
        if (message != null && !message.startsWith(RChitchat.getServerId() + "$")) {
            Bukkit.broadcastMessage(message.substring(message.indexOf('$') + 1));
        }
    }
}

package com.demigodsrpg.chitchat.redis;

import com.demigodsrpg.chitchat.RChitchat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redisson.core.MessageListener;

public class RedisChatListener implements MessageListener<String> {
    private final RChitchat R_INST;

    public RedisChatListener(RChitchat rInst) {
        R_INST = rInst;
    }

    @Override
    public void onMessage(String ignored, String message) {
        if (message != null && !message.startsWith(R_INST.getServerId() + "$")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message.substring(message.indexOf('$') + 1));
            }
        }
    }
}

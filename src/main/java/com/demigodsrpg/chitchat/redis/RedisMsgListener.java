package com.demigodsrpg.chitchat.redis;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchat.PrivateMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.redisson.core.MessageListener;

public class RedisMsgListener implements MessageListener<String> {
    private final Chitchat INST;

    public RedisMsgListener(Chitchat inst) {
        INST = inst;
    }

    @Override
    public void onMessage(String json) {
        PrivateMessage message = new PrivateMessage(INST, json);
        INST.getLogger().info(message.getLogMessage());
        OfflinePlayer offline = Bukkit.getOfflinePlayer(message.getTarget());
        if (offline.isOnline()) {
            offline.getPlayer().sendMessage(message.getFormattedMessage(false));
        }
    }
}
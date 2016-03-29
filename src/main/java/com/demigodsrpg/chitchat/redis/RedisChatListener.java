package com.demigodsrpg.chitchat.redis;

import cn.nukkit.Player;
import cn.nukkit.Server;
import com.demigodsrpg.chitchat.RChitchat;
import org.redisson.core.MessageListener;

public class RedisChatListener implements MessageListener<String> {
    private final RChitchat R_INST;

    public RedisChatListener(RChitchat rInst) {
        R_INST = rInst;
    }

    @Override
    public void onMessage(String ignored, String message) {
        if (message != null && !message.startsWith(R_INST.getServerId() + "$")) {
            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                player.sendMessage(message.substring(message.indexOf('$') + 1));
            }
        }
    }
}

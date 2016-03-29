package com.demigodsrpg.chitchat.redis;

import cn.nukkit.IPlayer;
import cn.nukkit.Server;
import com.demigodsrpg.chitchat.ChitchatPlugin;
import com.demigodsrpg.chitchat.PrivateMessage;
import org.redisson.core.MessageListener;

public class RedisMsgListener implements MessageListener<String> {
    private final ChitchatPlugin INST;

    public RedisMsgListener(ChitchatPlugin inst) {
        INST = inst;
    }

    @Override
    public void onMessage(String ignored, String json) {
        PrivateMessage message = new PrivateMessage(INST, json);
        INST.getLogger().info(message.getLogMessage());
        IPlayer offline = Server.getInstance().getOfflinePlayer(message.getTarget());
        if (offline.isOnline()) {
            offline.getPlayer().sendMessage(message.getFormattedMessage(false));
        }
    }
}
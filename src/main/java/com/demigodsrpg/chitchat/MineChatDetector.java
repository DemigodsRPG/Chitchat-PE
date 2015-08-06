package com.demigodsrpg.chitchat;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MineChatDetector implements Listener {
    static final List<String> USING_MINECHAT = new ArrayList<>();
    static final Cache<String, Long> JOINED_RECENTLY = CacheBuilder.newBuilder().concurrencyLevel(4).
            expireAfterWrite(3, TimeUnit.SECONDS).build();
    static final String[] MINE_SPAM = new String[]{"connected with a", "using minechat"};

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        JOINED_RECENTLY.put(event.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerJoinEvent event) {
        String playerId = event.getPlayer().getUniqueId().toString();
        if (USING_MINECHAT.contains(playerId)) {
            USING_MINECHAT.remove(playerId);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        String playerId = event.getPlayer().getUniqueId().toString();
        if (JOINED_RECENTLY.asMap().containsKey(playerId)) {
            String message = event.getMessage().toLowerCase();
            if (message.startsWith(MINE_SPAM[0]) && message.endsWith(MINE_SPAM[1])) {
                USING_MINECHAT.add(playerId);
                event.setCancelled(true);
            }
        }
    }
}

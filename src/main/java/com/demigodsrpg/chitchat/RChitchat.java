package com.demigodsrpg.chitchat;

import com.demigodsrpg.chitchat.redis.RedisChatListener;
import com.demigodsrpg.chitchat.redis.RedisMsgListener;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.RTopic;

public class RChitchat {
    // -- REDIS DATA -- //
    private static RChitchat INST;
    private final Redisson REDIS;
    static RTopic<String> REDIS_CHAT;
    static RTopic<String> REDIS_MSG;

    // -- OPTIONS -- //

    private static String SERVER_CHANNEL;
    private static String SERVER_ID;

    public RChitchat(ChitchatPlugin cc) {
        // Define the instance
        INST = this;

        // Get the server's id and chat channel
        SERVER_ID = cc.getConfig().getString("redis.server_id", "minecraft");
        SERVER_CHANNEL = cc.getConfig().getString("redis.channel", "default");

        // Configure and connect to redis
        Config config = new Config();
        config.useSingleServer().setAddress(cc.getConfig().getString("redis.connection", "127.0.0.1:6379"));
        REDIS = Redisson.create(config);

        // Setup mute map
        cc.MUTE_MAP = REDIS.getMap("mute.map");

        // Setup reply map
        cc.REPLY_MAP = REDIS.getMap("reply.map");

        // Setup msg topic
        REDIS_MSG = REDIS.getTopic("msg.topic");

        // Setup chat topic
        REDIS_CHAT = REDIS.getTopic(SERVER_CHANNEL + "$" + "chat.topic");

        // Make sure everything connected, if not, disable the plugin
        try {
            // Check if redis connected
            REDIS.getBucket("test").exists();
            cc.getLogger().info("Redis connection was successful.");

            // Register the msg listener
            REDIS_CHAT.addListener(new RedisChatListener(this));

            // Register the msg listener
            REDIS_MSG.addListener(new RedisMsgListener(cc));
        } catch (Exception ignored) {
            cc.getLogger().critical("Redis connection was unsuccessful!");
            cc.getLogger().critical("Disabling all Redis features.");
            cc.USE_REDIS = false;
        }
    }

    public String getServerChannel() {
        return SERVER_CHANNEL;
    }

    public String getServerId() {
        return SERVER_ID;
    }

    public static RChitchat getInst() {
        return INST;
    }
}

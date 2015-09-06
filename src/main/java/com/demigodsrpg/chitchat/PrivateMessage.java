package com.demigodsrpg.chitchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PrivateMessage implements Serializable {
    // -- SERIAL VERSION UID -- //

    public static final long serialVersionUID = 1L;

    // -- TRANSIENT DATA -- //

    private final transient Chitchat INST;

    // -- META DATA -- //

    private String target;
    private String sender;
    private String message;

    // -- CONSTRUCTORS -- //

    @SuppressWarnings("unchecked")
    public PrivateMessage(Chitchat inst, String json) {
        INST = inst;
        Gson gson = new GsonBuilder().create();
        Map<String, Object> map = gson.fromJson(json, Map.class);
        target = map.get("target").toString();
        sender = map.get("sender").toString();
        message = map.get("message").toString();
    }

    public PrivateMessage(Chitchat inst, String target, String sender, String message) {
        INST = inst;
        this.target = target;
        this.sender = sender;
        this.message = message;
    }

    // -- GETTERS -- //

    public String getTarget() {
        return target;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage(boolean out) {
        return ChatColor.DARK_GRAY + "PM" + (out ? " to" : " from") + " <" + ChatColor.DARK_AQUA +
                (out ? target : sender) + ChatColor.DARK_GRAY + ">: " + ChatColor.GRAY + message;
    }

    public String getLogMessage() {
        return "PM <" + sender + " to " + target + ">: " + message;
    }

    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("target", target);
        map.put("sender", sender);
        map.put("message", message);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(map, Map.class);
    }

    // -- SEND -- //

    public void send() {
        // Get the sender
        OfflinePlayer sender = Bukkit.getOfflinePlayer(this.sender);

        // Check if the player is on this server
        if (Bukkit.getPlayer(target) != null) {
            Bukkit.getPlayer(target).sendMessage(getFormattedMessage(false));
            Chitchat.getInst().getLogger().info(getLogMessage());
        } else if (Chitchat.getInst().USE_REDIS) {
            // Nope, send through redis
            RChitchat.REDIS_MSG.publish(toJson());
        } else if (sender.isOnline()) {
            // Something went wrong
            sender.getPlayer().sendMessage(ChatColor.RED + "There was an error sending a private message.");
            return;
        }

        // Send the 'sent' message
        if (sender.isOnline()) {
            sender.getPlayer().sendMessage(getFormattedMessage(true));
        }

        // Add to the reply map
        INST.getReplyMap().put(this.sender, this.target);
        INST.getReplyMap().put(this.target, this.sender);
    }
}

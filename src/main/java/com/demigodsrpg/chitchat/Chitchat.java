package com.demigodsrpg.chitchat;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.demigodsrpg.chitchat.format.ChatFormat;
import com.demigodsrpg.chitchat.tag.DefaultPlayerTag;
import com.demigodsrpg.chitchat.tag.NameTag;
import com.demigodsrpg.chitchat.tag.SpecificPlayerTag;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

public class Chitchat {
    static void useExamples(ChitchatPlugin chitchat) {
        TextComponent admin = new TextComponent("[A]");
        admin.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
        admin.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Administrator").
                color(net.md_5.bungee.api.ChatColor.DARK_RED).create()));
        TextComponent dev = new TextComponent("[D]");
        dev.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
        dev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Developer").
                color(net.md_5.bungee.api.ChatColor.DARK_GRAY).create()));
        chitchat.FORMAT/*.add(new WorldPlayerTag())*/
                .add(new DefaultPlayerTag("example-prefix", "chitchat.admin", admin, 3))
                .add(new SpecificPlayerTag("hqm", "HmmmQM", dev, 3))
                .add(new NameTag());
    }

    // -- STATIC API METHODS -- //

    /**
     * Get the chat format for adding tags or changing other settings.
     *
     * @return The enabled chat format.
     */
    public static ChatFormat getChatFormat() {
        return getInst().FORMAT;
    }

    /**
     * Set the entire chat format to a custom version.
     *
     * @param chatFormat A custom chat format.
     * @deprecated Only use this if you know what you are doing.
     */
    @Deprecated
    public static void setChatFormat(ChatFormat chatFormat) {
        getInst().FORMAT = chatFormat;
    }

    /**
     * Get the instance of this plugin.
     *
     * @return The current instance of this plugin.
     */
    public static ChitchatPlugin getInst() {
        return ChitchatPlugin.INST;
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     */
    @SuppressWarnings("unchecked")
    public static void sendMessage(BaseComponent message) {
        if (getInst().USE_REDIS) {
            RChitchat.REDIS_CHAT.publish(RChitchat.getInst().getServerId() + "$" + message.toLegacyText());
        }
        sendMessage(message, (Collection<Player>) Server.getInstance().getOnlinePlayers());
    }

    /**
     * Send a message through the Chitchat plugin, exclusive to a list of recipients.
     *
     * @param message    The message to be sent.
     * @param recipients The recipients of this message.
     */
    public static void sendMessage(BaseComponent message, Collection<? extends CommandSender> recipients) {
        for (CommandSender player : recipients) {
            player.sendMessage(message.toLegacyText()); // TODO Does PE support components?
        }
    }

    /**
     * Send a message through the Chitchat plugin. Includes the redis chat channel.
     *
     * @param message The message to be sent.
     * @deprecated This method is depreciated in favor of the new BaseComponent based method.
     */
    @Deprecated
    public static void sendMessage(String message) {
        sendMessage(new TextComponent(TextComponent.fromLegacyText(message)));
    }
}

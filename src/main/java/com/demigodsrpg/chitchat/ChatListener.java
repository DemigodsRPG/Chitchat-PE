package com.demigodsrpg.chitchat;

import com.demigodsrpg.chitchat.tag.ChatScope;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

    private final Chitchat INST;

    public ChatListener(Chitchat inst) {
        INST = inst;
    }

    // -- BUKKIT CHAT LISTENER -- //

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent chat) {
        if (INST.getMuteMap().keySet().contains(chat.getPlayer().getUniqueId().toString())) {
            chat.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFinalChat(AsyncPlayerChatEvent chat) {
        Chitchat.sendMessage(INST.FORMAT.getFormattedMessage(chat.getPlayer(), ChatScope.LOCAL, chat.getMessage()),
                chat.getRecipients());
        if (Chitchat.getInst().USE_REDIS && !INST.FORMAT.shouldCancelRedis(chat.getPlayer())) {
            RChitchat.REDIS_CHAT.publishAsync(RChitchat.getInst().getServerId() + "$" +
                    INST.FORMAT.getFormattedMessage(chat.getPlayer(), ChatScope.CHANNEL, chat.getMessage()).
                            toLegacyText());
        }
        chat.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreprocessCommand(PlayerCommandPreprocessEvent command) {
        Player player = command.getPlayer();
        String[] commandMsg = command.getMessage().split("\\s+");

        // Muted commands
        if (INST.getMuteMap().keySet().contains(player.getUniqueId().toString())) {
            if (Chitchat.getInst().MUTED_COMMANDS.contains(commandMsg[0].toLowerCase().substring(1))) {
                command.setCancelled(true);
                player.sendMessage(ChatColor.RED + "I'm sorry " + player.getName() + ", I'm afraid I can't do that.");
            }
        }

        // /me <message>
        else if (Chitchat.getInst().OVERRIDE_ME && commandMsg.length > 1 && commandMsg[0].equals("/me")) {
            command.setCancelled(true);
            if (Chitchat.getInst().MUTED_COMMANDS.contains("me") && INST.getMuteMap().keySet().contains(player.
                    getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "I'm sorry " + player.getName() + ", I'm afraid I can't do that.");
            } else {
                String message = command.getMessage().substring(1);
                message = ChatColor.ITALIC + ChatColor.stripColor(player.getDisplayName() + " " + message.substring(3));
                Chitchat.sendMessage(new TextComponent(message));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        // Remove all useless reply data
        String playerName = event.getPlayer().getName();
        INST.getReplyMap().remove(playerName);
        if (INST.getReplyMap().containsValue(playerName)) {
            INST.getReplyMap().entrySet().stream().
                    filter(entry -> entry.getValue().equals(playerName)).
                    forEach(entry -> INST.getReplyMap().remove(entry.getKey()));
        }
    }
}

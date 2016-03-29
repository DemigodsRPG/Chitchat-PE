package com.demigodsrpg.chitchat;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import com.demigodsrpg.chitchat.tag.ChatScope;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatListener implements Listener {

    private final ChitchatPlugin INST;

    public ChatListener(ChitchatPlugin inst) {
        INST = inst;
    }

    // -- BUKKIT CHAT LISTENER -- //

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(PlayerChatEvent chat) {
        if (INST.getMuteMap().keySet().contains(chat.getPlayer().getUniqueId().toString())) {
            chat.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFinalChat(PlayerChatEvent chat) {
        Chitchat.sendMessage(INST.FORMAT.getFormattedMessage(chat.getPlayer(), ChatScope.LOCAL, chat.getMessage()),
                chat.getRecipients());
        if (ChitchatPlugin.getInst().USE_REDIS && !INST.FORMAT.shouldCancelRedis(chat.getPlayer())) {
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
            if (ChitchatPlugin.getInst().MUTED_COMMANDS.contains(commandMsg[0].toLowerCase().substring(1))) {
                command.setCancelled(true);
                player.sendMessage(ChatColor.RED + "I'm sorry " + player.getName() + ", I'm afraid I can't do that.");
            }
        }

        // /me <message>
        else if (ChitchatPlugin.getInst().OVERRIDE_ME && commandMsg.length > 1 && commandMsg[0].equals("/me")) {
            command.setCancelled(true);
            if (ChitchatPlugin.getInst().MUTED_COMMANDS.contains("me") && INST.getMuteMap().keySet().contains(player.
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

package com.demigodsrpg.chitchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
class TitleUtil {
    final String NMS;
    final String CB;

    final Class<? extends Player> CB_CRAFTPLAYER;
    final Class NMS_ENTITY_PLAYER;
    final Class NMS_PLAYER_CONN;
    final Class NMS_ICHAT_BASE;
    final Class NMS_PACKET;
    final Class NMS_PACKET_PLAY_TITLE;
    final Class<? extends Enum> NMS_TITLE_ACTION;
    final Class NMS_CHAT_SERIALIZER;

    final Method GET_HANDLE;
    final Method SEND_PACKET;
    final Method ICHAT_A;

    final Field PLAYER_CONN;

    TitleUtil() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";

        // Common classpaths
        NMS = "net.minecraft.server." + version;
        CB = "org.bukkit.craftbukkit." + version;

        // Classes being used
        CB_CRAFTPLAYER = (Class<? extends Player>) Class.forName(CB + "entity.CraftPlayer");
        NMS_ENTITY_PLAYER = Class.forName(NMS + "EntityPlayer");
        NMS_PLAYER_CONN = Class.forName(NMS + "PlayerConnection");
        NMS_ICHAT_BASE = Class.forName(NMS + "IChatBaseComponent");
        NMS_PACKET = Class.forName(NMS + "Packet");
        NMS_PACKET_PLAY_TITLE = Class.forName(NMS + "PacketPlayOutTitle");
        NMS_TITLE_ACTION = (Class<? extends Enum>) Class.forName(NMS + "PacketPlayOutTitle$EnumTitleAction");
        NMS_CHAT_SERIALIZER = Class.forName(NMS + "IChatBaseComponent$ChatSerializer");

        // Methods being used
        GET_HANDLE = CB_CRAFTPLAYER.getMethod("getHandle");
        SEND_PACKET = NMS_PLAYER_CONN.getMethod("sendPacket", NMS_PACKET);
        ICHAT_A = NMS_CHAT_SERIALIZER.getMethod("a", String.class);

        // Fields being used
        PLAYER_CONN = NMS_ENTITY_PLAYER.getDeclaredField("playerConnection");
    }

    void sendTitle(Player player, int fadeInTicks, int stayTicks, int fadeOutTicks, String title, String subtitle) {
        try {
            Object craftPlayer = CB_CRAFTPLAYER.cast(player);
            Object entityPlayer = GET_HANDLE.invoke(craftPlayer);
            Object connection = PLAYER_CONN.get(entityPlayer);

            Object packetPlayOutTimes = NMS_PACKET_PLAY_TITLE.getConstructor(NMS_TITLE_ACTION, NMS_ICHAT_BASE, Integer.TYPE, Integer.TYPE, Integer.TYPE).
                    newInstance(Enum.valueOf(NMS_TITLE_ACTION, "TIMES"), null, fadeInTicks, stayTicks, fadeOutTicks);
            SEND_PACKET.invoke(connection, packetPlayOutTimes);

            if (subtitle != null) {
                subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
                subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
                Object titleSub = ICHAT_A.invoke(null, "{\"text\": \"" + subtitle + "\"}");
                Object packetPlayOutSubTitle = NMS_PACKET_PLAY_TITLE.getConstructor(NMS_TITLE_ACTION, NMS_ICHAT_BASE).
                        newInstance(Enum.valueOf(NMS_TITLE_ACTION, "SUBTITLE"), titleSub);
                SEND_PACKET.invoke(connection, packetPlayOutSubTitle);
            }

            if (title != null) {
                title = title.replaceAll("%player%", player.getDisplayName());
                title = ChatColor.translateAlternateColorCodes('&', title);
                Object titleMain = ICHAT_A.invoke(null, "{\"text\": \"" + title + "\"}");
                Object packetPlayOutTitle = NMS_PACKET_PLAY_TITLE.getConstructor(NMS_TITLE_ACTION, NMS_ICHAT_BASE).
                        newInstance(Enum.valueOf(NMS_TITLE_ACTION, "TIMES"), titleMain);
                SEND_PACKET.invoke(connection, packetPlayOutTitle);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException oops) {
            oops.printStackTrace();
        }
    }
}

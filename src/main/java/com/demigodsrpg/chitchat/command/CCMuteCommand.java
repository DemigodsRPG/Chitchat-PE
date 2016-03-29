package com.demigodsrpg.chitchat.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import com.demigodsrpg.chitchat.ChitchatPlugin;
import com.demigodsrpg.chitchat.util.JsonFileUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.TimeUnit;

public class CCMuteCommand implements CommandExecutor {
    private final ChitchatPlugin INST;
    private final JsonFileUtil JSON;

    public CCMuteCommand(ChitchatPlugin inst, JsonFileUtil util) {
        INST = inst;
        JSON = util;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length > 0) {
                if (command.getName().equals("ccmute")) {
                    /*if ("list".equalsIgnoreCase(args[0])) {
                        ((Player) sender).performCommand("ccmutelist");
                        return true;
                    }*/
                    Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                        String mutedId = Server.getInstance().getOfflinePlayer(args[0]).getName();
                        if (!INST.getMuteMap().containsKey(mutedId)) {
                            try {
                                INST.getMuteMap().put(mutedId, (double) System.currentTimeMillis() +
                                        argsToMilliseconds(args));
                                if (INST.savingMutes()) {
                                    JSON.saveToFile("mutes", INST.getMuteMap());
                                }
                                sender.sendMessage(ChatColor.YELLOW + "Muted " + args[0]);
                            } catch (IllegalArgumentException oops) {
                                sender.sendMessage(ChatColor.RED + args[2].toUpperCase() +
                                        " is an unsupported unit of time, try again.");
                            } catch (Exception oops) {
                                sender.sendMessage(ChatColor.RED + args[0] + " does not exist, try again.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "That player is already muted.");
                        }
                    }, 1, true);
                } else {
                    Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                        String mutedId = Server.getInstance().getOfflinePlayer(args[0]).getName();
                        if (INST.getMuteMap().containsKey(mutedId)) {
                            try {
                                INST.getMuteMap().remove(mutedId);
                                sender.sendMessage(ChatColor.YELLOW + "Unmuted " + args[0]);
                            } catch (Exception oops) {
                                sender.sendMessage(ChatColor.RED + args[0] + " does not exist, try again.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "That player isn't currently muted.");
                        }
                    }, 1, true);
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
        }
        return true;
    }

    private long argsToMilliseconds(String[] args) throws IllegalArgumentException {
        try {
            // Establish the value
            long val = Integer.valueOf(args[1]);

            // Only accept values in these bounds
            if (val > 0 || val <= 600) {
                // Grab the unit
                String unit = args[2];
                if (!unit.toUpperCase().endsWith("S")) {
                    unit += "S";
                }

                if (unit.equalsIgnoreCase("WEEKS")) {
                    unit = "DAYS";
                    val *= 7;
                } else if (unit.equalsIgnoreCase("YEARS")) {
                    unit = "DAYS";
                    val *= 365;
                }

                // Convert to milliseconds
                return TimeUnit.valueOf(unit.toUpperCase()).toMillis(val);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) {
        }

        // Default to 15 minutes
        return 900_000L;
    }
}

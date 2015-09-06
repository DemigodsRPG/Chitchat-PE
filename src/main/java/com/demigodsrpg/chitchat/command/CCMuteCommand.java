package com.demigodsrpg.chitchat.command;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchat.util.JsonFileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CCMuteCommand implements TabExecutor {
    private final Chitchat INST;
    private final JsonFileUtil JSON;

    public CCMuteCommand(Chitchat inst, JsonFileUtil util) {
        INST = inst;
        JSON = util;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length > 0) {
                if (command.getName().equals("ccmute")) {
                    if ("list".equalsIgnoreCase(args[0])) {
                        ((Player) sender).performCommand("ccmutelist");
                        return true;
                    }
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(INST, () -> {
                        String mutedId = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
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
                    });
                } else {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(INST, () -> {
                        String mutedId = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
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
                    });
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> guess = new ArrayList<>();
        if (sender instanceof Player && sender.hasPermission("chitchat.mute")) {
            if (args.length == 1) {
                if (command.getName().equals("ccunmute")) {
                    guess.addAll(INST.getMuteMap().keySet().stream().map(UUID::fromString).
                            map(Bukkit::getOfflinePlayer).filter(muted -> muted.getName() != null && muted.getName().
                            toLowerCase().startsWith(args[0].toLowerCase())).map(OfflinePlayer::getName).
                            collect(Collectors.toList()));
                } else {
                    guess.addAll(Bukkit.getOnlinePlayers().stream().
                            filter(online -> online.getName().toLowerCase().startsWith(args[0].toLowerCase())).
                            map(Player::getName).collect(Collectors.toList()));
                }
            }
        }
        return guess;
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

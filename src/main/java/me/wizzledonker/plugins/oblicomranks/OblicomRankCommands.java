/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Winfried
 */
public class OblicomRankCommands implements CommandExecutor {
    
    private OblicomRanks plugin;
    
    public OblicomRankCommands(OblicomRanks instance) {
        plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("score")) {
            if (args.length == 1) {
                if (!sender.hasPermission("oblicom.ranks.scoreothers")) {
                    sender.sendMessage(ChatColor.RED + "You're not allowed to check the score of others.");
                    return true;
                }
                boolean online = false;
                for (Player play : plugin.getServer().getOnlinePlayers()) {
                    if (play.getName().equals(args[0])) {
                        online = true;
                        break;
                    }
                }
                if (!online) {
                    sender.sendMessage(ChatColor.RED + "Player isn't online");
                    return true;
                }
                sender.sendMessage(plugin.score.getRankInfo(plugin.getServer().getPlayer(args[0])));
                return true;
            } else {
                if (sender instanceof Player) {
                    sender.sendMessage(plugin.score.getRankInfo((Player) sender));
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can check their progress!");
                }
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("addscore")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("oblicom.ranks.addscore")) {
                    return true;
                }
            }
            if (args.length == 2) {
                boolean online = false;
                int amount = 500;
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().equals(args[0])) {
                        online = true;
                        break;
                    }
                }
                if (!online) {
                    sender.sendMessage(ChatColor.RED + "Player isn't online");
                    return true;
                }
                try { 
                    amount = Integer.parseInt(args[1]); 
                } catch(NumberFormatException e) { 
                    return false; 
                }
                plugin.score.addScore(amount, plugin.getServer().getPlayer(args[0]));
                sender.sendMessage(ChatColor.GREEN + args[1] + " was added to " + args[0] + "'s score");
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("subscore")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("oblicom.ranks.subscore")) {
                    return true;
                }
            }
            if (args.length == 2) {
                boolean online = false;
                int amount = 500;
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().equals(args[0])) {
                        online = true;
                        break;
                    }
                }
                if (!online) {
                    sender.sendMessage(ChatColor.RED + "Player isn't online");
                    return true;
                }
                try { 
                    amount = Integer.parseInt(args[1]); 
                } catch(NumberFormatException e) { 
                    return false; 
                }
                plugin.score.subtractScore(amount, plugin.getServer().getPlayer(args[0]));
                sender.sendMessage(ChatColor.GREEN + args[1] + " was subtracted from " + args[0] + "'s score");
                return true;
            }
        }
        return false;
    }
    
}

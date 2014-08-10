/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Winfried
 */
public class OblicomRankScore {
    private static OblicomRanks plugin;
    
    private File scoreConfigFile = null;
    private FileConfiguration scoreConfig = null;
    
    public OblicomRankScore(OblicomRanks instance) {
        plugin = instance;
    }
    
    private void reloadScoreConfig() {
        if (scoreConfigFile == null) {
            scoreConfigFile = new File(plugin.getDataFolder(), "scores.yml");
        }
        scoreConfig = YamlConfiguration.loadConfiguration(scoreConfigFile);
    }
    
    private FileConfiguration getScoreConfig() {
        if (scoreConfig == null) {
            reloadScoreConfig();
        }
        return scoreConfig;
    }
    
    private void saveScoreConfig() {
        if (scoreConfigFile == null || scoreConfig == null) {
            return;
        }
        try {
            scoreConfig.save(scoreConfigFile);
        } catch (IOException ex) {
            plugin.log("oops! Error saving score config file. Here's the details: " + ex.toString());
        }
    }
    
    public int getScore(Player player) {
        return getScoreConfig().getInt(player.getUniqueId().toString() + ".score");
    }
    
    public void addScore(int add, Player player) {
        player.sendMessage(ChatColor.GREEN + "+" + add + " Points");
        if (getScoreConfig().contains(player.getUniqueId().toString())) {
            add = getScore(player) + add;
        }
        setScore(add, player);
    }
    
    public void subtractScore(int subtract, Player player) {
        player.sendMessage(ChatColor.RED + "-" + subtract + " Points");
        if (getScoreConfig().contains(player.getName())) {
            subtract = getScore(player) - subtract;
        }
        setScore(subtract, player);
    }
    
    public void setScore(int score, Player player) {
         getScoreConfig().set(player.getUniqueId().toString() + ".score", score);
         saveScoreConfig();
         updateRank(player);
    }
    
    public String determineFaction(Player player) {
        String group = plugin.permission.getPrimaryGroup(player);
        return plugin.getFaction(group);
    }
    
    public String getRankInfo(Player player) {
        //Update the player's rank
        updateRank(player);
        //Form the information string
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN).append(determineFaction(player)).append(" rank progress\n");
        sb.append(ChatColor.RED).append(getRank(player)).append(ChatColor.GOLD).append(" -");
        for (int i = 0; i < 50; ++i) {
            if (i == Math.round(getProgressTowardsNextRank(player) * 50)) {
                sb.append(ChatColor.DARK_RED);
            }
            sb.append('|');
        }
        sb.append(ChatColor.GOLD).append("- ").append(ChatColor.GREEN).append(plugin.getNextRank(determineFaction(player), getRank(player))).append("\n");
        sb.append(ChatColor.AQUA).append("The next rank grants you: \n").append(ChatColor.DARK_GREEN);
        if (plugin.getRankVariable(plugin.getNextRank(determineFaction(player), getRank(player)), "permissions") != null) {
            for (String perm : (List<String>) plugin.getRankVariable(plugin.getNextRank(determineFaction(player), getRank(player)), "permissions")) {
                sb.append("- ").append(perm).append("\n");
            }
        } else {
            sb.append("- Nothing");
        }
        return sb.toString();
    }
    
    public void updateRank(Player player) {
        setRank(player, getRank(player));
    }
    
    public String getRank(Player player) {
        int prev = 0;
        if (getScore(player) < prev) {
            return plugin.getLowestRank(determineFaction(player));
        }
        for (Iterator<Integer> it = plugin.getRanks(determineFaction(player)).keySet().iterator(); it.hasNext();) {
            int current = it.next();
            if (getScore(player) == current) {
                return plugin.getRanks(determineFaction(player)).get(current);
            }
            if (getScore(player) < current) {
                if (getScore(player) > prev) {
                    return plugin.getRanks(determineFaction(player)).get(prev);
                }
            } else if ((Integer) plugin.getRankVariable(plugin.getHighestRank(determineFaction(player)), "score") == current) {
                return plugin.getRanks(determineFaction(player)).get(current);
            }
            prev = current;
        }
        return plugin.getRanks(determineFaction(player)).get(prev);
    }
    
    public float getProgressTowardsNextRank(Player player) {
        //Problem is here, decimal is returning 0.0
        //Fixed and edited!
        float playerScore = (float) getScore(player);
        float playerRank = (float) ((Integer) plugin.getRankVariable(getRank(player), "score"));
        float nextPlayerRank = (float) ((Integer) plugin.getRankVariable(plugin.getNextRank(determineFaction(player), getRank(player)), "score"));
        
        float finalArith = (playerScore - playerRank) / (nextPlayerRank - playerRank);
        
        return finalArith;
    }
    
    private void setRank(Player player, String rank) {
        
        String prePrefix = determinePrefix(player);
        
        //Set the prefix for the player
        plugin.chat.setPlayerPrefix(player, (String) plugin.getRankVariable(rank, "prefix"));
        
        
        //Now that the easy part is done, Do the permissions!
        for (String perm : plugin.getAllRankPermissions()) {
            if (plugin.permission.playerHas(player, perm)) {
                plugin.permission.playerRemove(player, perm);
            }
        }
        if (plugin.getRankVariable(rank, "permissions") != null) {
            for (String perm : (List<String>) plugin.getRankVariable(rank, "permissions")) {
                plugin.permission.playerAdd(player, perm);
            }
        }
        if (prePrefix != determinePrefix(player)) {
            player.sendMessage(ChatColor.GREEN + "Your rank has changed to " + ChatColor.GOLD + rank);
            plugin.log("Player " + player.getName() + " was changed to " + rank);
        }

    }
    
    private String determinePrefix(Player player) {
        String finalPrefix = "";
            
        if (plugin.chat.getPlayerPrefix(player) != "") {
            finalPrefix = plugin.chat.getPlayerPrefix(player);
        } else {
            finalPrefix = plugin.chat.getGroupPrefix(player.getWorld(), plugin.permission.getPrimaryGroup(player));
        }
        
        return finalPrefix;
    }
    
}

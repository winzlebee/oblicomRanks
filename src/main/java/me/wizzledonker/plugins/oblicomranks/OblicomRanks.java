package me.wizzledonker.plugins.oblicomranks;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.RegisteredServiceProvider;

public class OblicomRanks extends JavaPlugin implements Listener {
    
    public Permission permission = null;
    public Chat chat = null;
    public OblicomRankScore score = new OblicomRankScore(this);
    public OblicomRankCommands commandex = new OblicomRankCommands(this);
    
    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here.
        log("Successfully unloaded");
    }

    @Override
    public void onEnable() {
        if (!setupPermissions()) {
            log("Failed to hook into permissions via Vault! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupChat()) {
            log("Failed to hook into chat via Vault! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("score").setExecutor(commandex);
        getCommand("addscore").setExecutor(commandex);
        getCommand("subscore").setExecutor(commandex);
        log("Successfully loaded.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        score.updateRank(event.getPlayer());
    }
    
    public void log(String text) {
        System.out.println("[" + this + "] " + text);
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    
    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }
    
    public String getFaction(String group) {
        for (Iterator<String> it = getConfig().getConfigurationSection("groups").getKeys(false).iterator(); it.hasNext();) {
            String key = it.next();
            if (getConfig().getStringList("groups." + key).contains(group)) {
                return key;
            }
        }
        return getConfig().getString("groups.default");
    }
    
    public Map<Integer, String> getRanks(String faction) {
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        if (!getConfig().contains("ranks." + faction)) {
            return null;
        }
        String currentRank = getLowestRank(faction);
        for (int i = 0; i < getConfig().getConfigurationSection("ranks." + faction).getKeys(false).size(); i++) {
            map.put((Integer) getRankVariable(currentRank, "score"), currentRank);
            currentRank = getNextRank(faction, currentRank);
        }
        return map;
    }
    
    public String getNextRank(String faction, String rank) {
        String possibleRank = getHighestRank(faction);
        for (String key : getConfig().getConfigurationSection("ranks." + faction).getKeys(false)) {
            if (key.equals(possibleRank)) continue;
            if (((Integer) getRankVariable(key, "score")) > ((Integer) getRankVariable(rank, "score"))) {
                if (((Integer) getRankVariable(key, "score")) < ((Integer) getRankVariable(possibleRank, "score"))) {
                    possibleRank = key;
                }
            }
        }
        return possibleRank;
    }
    
    public String getHighestRank(String faction) {
        String possibleRank = getConfig().getConfigurationSection("ranks." + faction).getKeys(false).iterator().next();
        for (String key : getConfig().getConfigurationSection("ranks." + faction).getKeys(false)) {
            if (((Integer) getRankVariable(key, "score")) > ((Integer) getRankVariable(possibleRank, "score"))) {
                possibleRank = key;
            }
        }
        return possibleRank;
    }
    
    public String getLowestRank(String faction) {
        String possibleRank = getConfig().getConfigurationSection("ranks." + faction).getKeys(false).iterator().next();
        for (String key : getConfig().getConfigurationSection("ranks." + faction).getKeys(false)) {
            if (((Integer) getRankVariable(key, "score")) < ((Integer) getRankVariable(possibleRank, "score"))) {
                possibleRank = key;
            }
        }
        return possibleRank;
    }
    
    public Object getRankVariable(String rank, String variable) {
        for (Iterator<String> it = getConfig().getConfigurationSection("ranks").getKeys(false).iterator(); it.hasNext();) {
            String key = it.next();
            if (getConfig().getConfigurationSection("ranks." + key).contains(rank)) {
                if (getConfig().getConfigurationSection("ranks." + key + "." + rank).contains(variable)) {
                    return getConfig().get("ranks." + key + "." + rank + "." + variable);
                }
            }
        }
        return null;
    }
    
    public Set<String> getAllRankPermissions() {
        Set<String> values = new HashSet<String>();
        
        for (String faction : getConfig().getConfigurationSection("ranks").getKeys(false)) {
            for (String group : getConfig().getConfigurationSection("ranks." + faction).getKeys(false)) {
                if (!getConfig().contains("ranks." + faction + "." + group + ".permissions")) {
                    continue;
                }
                for (Object s : getConfig().getList("ranks." + faction + "." + group + ".permissions")) {
                    values.add((String) s);
                }
            }
        }
        
        return values;
    }
    
    private void loadConfig() {
        FileConfiguration conf = this.getConfig();
        conf.options().copyDefaults(true);
        
        List<String> criminals = Arrays.asList("valenwayr", "mattropolis");
        SetIfNoDefaultObject(conf, "groups.criminal", criminals);
        List<String> police = Arrays.asList("oblicom", "traticom");
        SetIfNoDefaultObject(conf, "groups.police", police);
        List<String> citizen = Arrays.asList("citizen", "moderator");
        SetIfNoDefaultObject(conf, "groups.citizen", citizen);
        
        List<String> policePerms = Arrays.asList("oblicom.wanted.view");
        //Police Defaults
        SetIfNoDefaultObject(conf, "ranks.police.probationaryconstable.score", 0);
        SetIfNoDefaultObject(conf, "ranks.police.probationaryconstable.prefix", "PrC." + ChatColor.DARK_AQUA);
        SetIfNoDefaultObject(conf, "ranks.police.constable.score", 200);
        SetIfNoDefaultObject(conf, "ranks.police.constable.prefix", "Cons." + ChatColor.AQUA);
        SetIfNoDefaultObject(conf, "ranks.police.constable.permissions", policePerms);
        SetIfNoDefaultObject(conf, "ranks.police.seniorconstable.score", 500);
        SetIfNoDefaultObject(conf, "ranks.police.constable.prefix", "Cons." + ChatColor.LIGHT_PURPLE);
        
        //Criminal Defaults
        SetIfNoDefaultObject(conf, "ranks.criminal.assosciate.score", 0);
        SetIfNoDefaultObject(conf, "ranks.criminal.assosciate.prefix", "As." + ChatColor.GRAY);
        SetIfNoDefaultObject(conf, "ranks.criminal.soldato.score", 500);
        SetIfNoDefaultObject(conf, "ranks.criminal.soldato.prefix", "Sol." + ChatColor.YELLOW);
        SetIfNoDefaultObject(conf, "ranks.criminal.captain.score", 1000);
        SetIfNoDefaultObject(conf, "ranks.criminal.captain.prefix", "Capo." + ChatColor.RED);
        
        SetIfNoDefaultObject(conf, "ranks.citizen.neutral.score", 0);
        SetIfNoDefaultObject(conf, "groups.default", "citizen");
        
        saveConfig();
    }
    
    private static void SetIfNoDefaultObject(FileConfiguration config, String path, Object value){
        if(config == null) return;
        if(path == null) return;
        if(value == null) return;
       
        if(!config.contains(path)){
            config.set(path, value);
        }
    }
}


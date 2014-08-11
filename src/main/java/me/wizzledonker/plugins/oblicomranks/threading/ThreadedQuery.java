/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks.threading;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.wizzledonker.plugins.oblicomranks.OblicomRanks;
import me.wizzledonker.plugins.oblicomranks.events.ThreadFinished;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Winfried
 */
public class ThreadedQuery implements Runnable {
    
    private OblicomRanks plugin;
    
    private String uuid;
    
    private Player player;
    
    //Whether to show the result of the query to the player once complete
    private boolean display = false;
    
    private boolean getData = true;
    
    private int score = 0;
 
    private PreparedStatement sql = null;

    private ResultSet res = null;

    private boolean running = false;

    private Object[] returnedData;
    
    public ThreadedQuery(Player p, String playerUUID, int score, OblicomRanks instance) {
        //Constructor for changing data in the database (add variables to suit)
        this.getData = false;
        this.score = score;
        this.init(p, playerUUID, instance);
    }
    
    public ThreadedQuery(Player p, String playerUUID, OblicomRanks instance, boolean d) {
        //Constructor for getting data
        this.getData = true;
        this.display = d;
        this.init(p, playerUUID, instance);
    }
    
    private void init(Player p, String playerUUID, OblicomRanks instance) {
        this.uuid = playerUUID;
        this.player = p;
        
        Thread thread = new Thread(this);
        
        plugin = instance;
        
        thread.start();
    }

    @Override
    public void run() {
 
        this.running = true;

        Bukkit.getPluginManager().callEvent(new ThreadFinished(this.running, this, this.returnedData, this.player, this.display));
        Boolean contains = false;
        
        try {
            
            Statement statement = plugin.c.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS scores ( id MEDIUMINT NOT NULL AUTO_INCREMENT, player VARCHAR(255) NOT NULL, score INT, PRIMARY KEY id )");
            
            //Actual content of the loop/what it does
            sql = plugin.c.prepareStatement("SELECT * FROM 'scores' WHERE player = ? LIMIT 1;");
            sql.setString(1, uuid);
            
            res = sql.executeQuery();
            if (res.next()) {
                contains = true;
            }
            
            if (contains && this.getData) {
                //We are only getting data, so let's give it back in an event when needed
                PreparedStatement sql = plugin.c.prepareStatement("SELECT * FROM 'scores' WHERE player = ?;");
                sql.setString(1, uuid);
                res = sql.executeQuery();
                res.next();
                Object[] s = {res.getString("player"), res.getString("score")};
                this.returnedData = s;
            } else {
                //If not, we're setting that value for the player in the MySQL database, let's do it!
                PreparedStatement sql = plugin.c.prepareStatement("INSERT INTO 'scores' (player, score) VALUES (?, ?);");
                sql.setString(1, uuid);
                sql.setInt(2, this.score);
                
                res = sql.executeQuery();
                Object[] s = {res.getString("player"), res.getString("score")};
                this.returnedData = s;
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (sql != null) {
                    sql.close();
                }
                
                if (res != null) {
                    res.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ThreadedQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        this.running = false;

        Bukkit.getPluginManager().callEvent(new ThreadFinished(this.running, this, this.returnedData, this.player, this.display));
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks.listeners;

import me.wizzledonker.plugins.oblicomranks.OblicomRanks;
import me.wizzledonker.plugins.oblicomranks.events.ThreadFinished;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Winfried
 */
public class ThreadListener implements Listener {
    
    private static OblicomRanks plugin;
    
    public ThreadListener(OblicomRanks instance) {
        plugin = instance;
    }
    
    @EventHandler
    public void onThreadFinished(ThreadFinished event) {
        if (!event.getRunning()) {
            
        }
    }
    
}

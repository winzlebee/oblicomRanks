/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks.events;

import me.wizzledonker.plugins.oblicomranks.threading.ThreadedQuery;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Winfried
 */
public class ThreadFinished extends Event {
    private static HandlerList handlers = new HandlerList();

    private boolean running;
    private boolean displayToPlayer;
    
    private Player player;

    private ThreadedQuery thread;

    private Object[] data;

    public ThreadFinished(boolean running, ThreadedQuery thread, Object[] data, Player p, boolean display) {

        this.running = running;
        this.displayToPlayer = display;
        
        this.player = p;

        this.thread = thread;

        this.data = data;

    }

    public HandlerList getHandlers() {
        
        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

    public boolean getRunning() {

        return this.running;

    }

    public ThreadedQuery getThread() {

        return this.thread;

    }

    public Object[] getDataProcessed() { return this.data; }
    
    public Player getPlayer() { return this.player; }
    public boolean displayToPlayer() { return this.displayToPlayer; }

}

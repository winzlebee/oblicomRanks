/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.wizzledonker.plugins.oblicomranks.events;

import me.wizzledonker.plugins.oblicomranks.threading.ThreadedQuery;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Winfried
 */
public class ThreadFinished extends Event {
    private static HandlerList handlers = new HandlerList();

    private boolean running;

    private ThreadedQuery thread;

    private Object[] data;

    public ThreadFinished(boolean running, ThreadedQuery thread, Object[] data) {

        this.running = running;

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

}

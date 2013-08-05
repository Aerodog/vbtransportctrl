/*
 * Copyright (c) 2013 Patrick Anker and contributors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.patrickanker.vbtransportctrl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;

public class MinecartTrackerThread extends Thread {
    
    private boolean terminate = false;
    private List<Minecart> activeMinecarts = new LinkedList<Minecart>();
    private Map<Minecart, Location> locations = new HashMap<Minecart, Location>();
    
    private List<Minecart> activateQueue = new LinkedList<Minecart>();
    private List<Minecart> deactivateQueue = new LinkedList<Minecart>();
    
    public MinecartTrackerThread()
    {
        this.setName("Minecart Tracker Thread");
    }
    
    /* ONLY CALL THIS ON STARTUP TO CATCH ACTIVE CARTS THROUGH A RELOAD */
    public void reload(Minecart cart)
    {
        activeMinecarts.add(cart);
    }
    
    @Override
    public void run()
    {
        TrainStopTrackerThread stopManager = TransportPlugin.getTrainStopManager();
        
        while (!terminate) {
            if (!deactivateQueue.isEmpty())
                activeMinecarts.removeAll(deactivateQueue);
            
            for (Minecart cart : deactivateQueue) {
                if (stopManager.isTrainStopRegistered(cart.getLocation())) {
                    TrainStop _stop = stopManager.getTrainStopForLocation(cart.getLocation());
                    
                    if (stopManager.isPendingStop(_stop)) {
                        stopManager.removePendingStop(_stop);
                    } else {
                        cart.remove();
                    }
                } else {
                    cart.remove();
                }
            }
            
            deactivateQueue.clear();
            
            if (!activateQueue.isEmpty()) {
                activeMinecarts.addAll(activateQueue);
            }
            
            activateQueue.clear();
            
            for (Minecart cart : activeMinecarts) {
                if (didMove(cart)) {
                    if (stopManager.isTrainStopRegistered(cart.getLocation())) {
                        TrainStop stop = stopManager.getTrainStopForLocation(cart.getLocation());
                        stop.halt(cart);
                        stopManager.addPendingStop(stop);
                    }
                    
                    if (TrainStop.isStop(cart.getLocation()) && !stopManager.isTrainStopRegistered(cart.getLocation())) {
                        TrainStop stop = new TrainStop(cart.getLocation());
                        stopManager.registerTrainStop(stop);
                        stop.halt(cart);
                        stopManager.addPendingStop(stop);
                    }
                }
            }
        }
        
        TransportPlugin.DEBUG("Neutralising thread resources: " + getName());
        
        activeMinecarts.clear();
        locations.clear();
        
        activateQueue.clear();
        deactivateQueue.clear();
        
        activeMinecarts = null;
        locations = null;
    }
    
    public void terminate()
    {
        terminate = true;
    }
    
    public void activateCart(Minecart cart)
    {
        activateQueue.add(cart);
    }
    
    public void deactivateCart(Minecart cart)
    {
        deactivateQueue.add(cart);
    }
    
    private boolean didMove(final Minecart cart)
    {
        // Movement is based on BLOCKS moved, not actual position... else this would ALWAYS be true...
        
        synchronized (cart) {
            Location previousLocation = locations.get(cart);

            if (previousLocation == null) {
                locations.put(cart, cart.getLocation());
                return false;
            }

            int x1 = previousLocation.getBlockX();
            int y1 = previousLocation.getBlockY();
            int z1 = previousLocation.getBlockZ();
            
            int x2 = cart.getLocation().getBlockX();
            int y2 = cart.getLocation().getBlockY();
            int z2 = cart.getLocation().getBlockZ();
            
            if (x1 != x2 || y1 != y2 || z1 != z2) {
                locations.put(cart, cart.getLocation());
                return true;
            }
            
            return false;
        }
    }
}

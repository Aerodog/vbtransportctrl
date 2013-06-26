/*
 * MinecartTrackerThread.java
 *
 * Project: vbtransportctrl
 *
 * Copyright (C) Patrick Anker 2013. All rights reserved.
 * 
 * vbtransportctrl by Patrick Anker is licensed under a Creative Commons 
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 *
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
        TrainStopTrackerThread _stopManager = TransportPlugin.getTrainStopManager();
        
        while (!terminate) {
            if (!deactivateQueue.isEmpty())
                activeMinecarts.removeAll(deactivateQueue);
            
            for (Minecart cart : deactivateQueue) {
                if (_stopManager.isTrainStopRegistered(cart.getLocation())) {
                    TrainStop _stop = _stopManager.getTrainStopForLocation(cart.getLocation());
                    
                    if (!_stopManager.isPendingStop(_stop)) {
                        _stopManager.removePendingStop(_stop);
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
                if (didMove(cart) && _stopManager.isTrainStopRegistered(cart.getLocation())) {
                    TrainStop _stop = _stopManager.getTrainStopForLocation(cart.getLocation());
                    
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
            
            if (x1 != x2) {
                locations.put(cart, cart.getLocation());
                return true;
            } else if (y1 != y2) {
                locations.put(cart, cart.getLocation());
                return true;
            } else if (z1 != z2) {
                locations.put(cart, cart.getLocation());
                return true;
            }
            
            return false;
        }
    }
}

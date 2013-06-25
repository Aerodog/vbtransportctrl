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
import org.bukkit.util.Vector;

public class MinecartTrackerThread extends Thread {
    
    private boolean terminate = false;
    private List<Minecart> activeMinecarts = new LinkedList<Minecart>();
    private Map<Minecart, Location> locations = new HashMap<Minecart, Location>();
    
    private List<Minecart> activateQueue = new LinkedList<Minecart>();
    private List<Minecart> deactivateQueue = new LinkedList<Minecart>();
    
    private Map<TrainStop, Long> pendingStops = new HashMap<TrainStop, Long>();
    
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
        while (!terminate) {
            if (!deactivateQueue.isEmpty())
                activeMinecarts.removeAll(deactivateQueue);
            
            for (Minecart cart : deactivateQueue) {
                if (StopDetector.isStop(cart.getLocation()) != null) {
                    TrainStop stop = StopDetector.isStop(cart.getLocation());
                    
                    if (isPendingStop(stop))
                        removePendingStop(stop);
                }
                
                cart.remove();
            }
            
            if (!activateQueue.isEmpty()) {
                activeMinecarts.addAll(activateQueue);
                TransportPlugin.DEBUG("Loaded new carts into active carts");
            }
            
            activateQueue.clear();
            deactivateQueue.clear();
            
            
            for (Minecart cart : activeMinecarts) {
                if (didMove(cart)) {
                    TrainStop _stop = StopDetector.isStop(cart.getLocation());

                    if (_stop != null) {
                        TransportPlugin.DEBUG("Cart in stop");
                        
                        Location _previous = locations.get(cart);

                        if (_stop.inStop(_previous) && !_stop.inStop(cart.getLocation())) {
                            _stop.deactivateTracks();
                        } else if (!_stop.inStop(_previous) && _stop.inStop(cart.getLocation())) {
                            addPendingStop(_stop);
                        }
                    }
                }
            }
            
            for (Map.Entry<TrainStop, Long> entry : pendingStops.entrySet()) {
                long _entry = entry.getValue();
                
                if ((System.currentTimeMillis() - _entry) > 15000) {
                    entry.getKey().activateTracks();
                }
            }
        }
        
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
    
    public boolean isPendingStop(TrainStop stop)
    {
        return pendingStops.containsKey(stop);
    }
    
    public void addPendingStop(TrainStop stop)
    {
        pendingStops.put(stop, System.currentTimeMillis());
    }
    
    public void removePendingStop(TrainStop stop)
    {
        pendingStops.remove(stop);
    }
    
    private boolean didMove(final Minecart cart)
    {
        synchronized (cart) {
            Location previousLocation = locations.get(cart);

            if (previousLocation == null) {
                locations.put(cart, cart.getLocation());
                return false;
            }

            Vector vel = cart.getVelocity();

            if (vel.length() != 0) {
                locations.put(cart, cart.getLocation());
                return true;
            } else {
                return false;
            }
        }
    }
}

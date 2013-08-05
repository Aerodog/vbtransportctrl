/*
 * TrainStopTrackerThread.java
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
import org.bukkit.entity.Player;


public class TrainStopTrackerThread extends Thread {
    
    private boolean terminate = false;
    
    private Map<TrainStop, Long> activeTrainStops = new HashMap<TrainStop, Long>();
    private Map<TrainStop, Long> pendingStops = new HashMap<TrainStop, Long>();
    
    private List<TrainStop> activateQueue = new LinkedList<TrainStop>();
    private List<TrainStop> deactivateQueue = new LinkedList<TrainStop>();
    
    private List<TrainStop> pendingAddQueue = new LinkedList<TrainStop>();
    private List<TrainStop> pendingRemoveQueue = new LinkedList<TrainStop>();
    
    public TrainStopTrackerThread()
    {
        setName("Train Stop Tracker Thread");
    }
    
    @Override
    @SuppressWarnings("UnusedAssignment")
    public void run()
    {   
        while (!terminate) {
            
            for (TrainStop stop : deactivateQueue) {
                if (stop.hasWaitingCart()) {
                    stop.getWaitingCart().remove();
                    stop.setWaitingCart(null);
                }
                
                activeTrainStops.remove(stop);
                stop = null;
            }
            
            deactivateQueue.clear();
            
            if (!activateQueue.isEmpty()) {
                for (TrainStop stop : activateQueue) {
                    activeTrainStops.put(stop, System.currentTimeMillis());
                }
                
                activateQueue.clear();
            }
            
            for (Map.Entry<TrainStop, Long> entry : activeTrainStops.entrySet()) {
                if ((System.currentTimeMillis() - entry.getValue()) > 1800000) {
                    unregisterTrainStop(entry.getKey());
                }
            }
            
            for (TrainStop stop : pendingRemoveQueue) {
                if (stop.hasWaitingCart()) {
                    stop.getWaitingCart().remove();
                    stop.setWaitingCart(null);
                }
                
                pendingStops.remove(stop);
            }
            
            pendingRemoveQueue.clear();
            
            if (!pendingAddQueue.isEmpty()) {
                for (TrainStop stop : pendingAddQueue) {
                    pendingStops.put(stop, System.currentTimeMillis());
                }
                
                pendingAddQueue.clear();
            }
            
            for (Map.Entry<TrainStop, Long> entry : pendingStops.entrySet()) {
                if (entry.getKey().hasWaitingCart()) {
                    Minecart cart = entry.getKey().getWaitingCart();
                    
                    if (System.currentTimeMillis() - entry.getValue() > (entry.getKey().getTimeout() * 1000)) {
                        if (cart.getPassenger() == null) {
                            cart.remove();
                            removePendingStop(entry.getKey());
                        } else if (cart.getPassenger() != null && cart.getPassenger() instanceof Player) {
                            entry.getKey().deploy();
                            removePendingStop(entry.getKey());
                        } else {
                            // Minecart tomfoolery
                            cart.remove();
                            removePendingStop(entry.getKey());
                        }
                    }
                }
            }
        }
        
        TransportPlugin.DEBUG("Neutralising thread resources: " + getName());
        
        activeTrainStops.clear();
        pendingStops.clear();
        
        activateQueue.clear();
        deactivateQueue.clear();
        
        pendingAddQueue.clear();
        pendingRemoveQueue.clear();
        
        activeTrainStops = null;
        pendingStops = null;
    }
    
    public void terminate()
    {
        terminate = true;
    }
    
    public boolean isTrainStopRegistered(Location loc)
    {
        for (TrainStop stop : getTrainStops()) {
            if (loc.equals(stop.getTrackLocation()))
                return true;
        }
        
        return false;
    }
    
    public TrainStop getTrainStopForLocation(Location loc)
    {
        for (TrainStop stop : getTrainStops()) {
            if (loc.equals(stop.getTrackLocation()))
                return stop;
        }
        
        return null;
    }
    
    public boolean isTrainStopRegistered(TrainStop stop)
    {
        return activeTrainStops.containsKey(stop);
    }
    
    public void registerTrainStop(TrainStop stop)
    {
        activateQueue.add(stop);
    }
    
    public void unregisterTrainStop(TrainStop stop)
    {
        deactivateQueue.add(stop);
    }
    
    public void updateStopTime(TrainStop stop)
    {
        activeTrainStops.put(stop, System.currentTimeMillis());
    }
    
    public boolean isPendingStop(TrainStop stop)
    {
        return pendingStops.containsKey(stop);
    }
    
    public void addPendingStop(TrainStop stop)
    {
        pendingAddQueue.add(stop);
    }
    
    public void removePendingStop(TrainStop stop)
    {
        pendingRemoveQueue.add(stop);
    }
    
    private List<TrainStop> getTrainStops()
    {
        LinkedList<TrainStop> l = new LinkedList<TrainStop>();
        
        for (Map.Entry<TrainStop, Long> entry : activeTrainStops.entrySet()) {
            l.add(entry.getKey());
        }
        
        return l;
    }
}

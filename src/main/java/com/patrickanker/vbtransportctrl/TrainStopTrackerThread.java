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


public class TrainStopTrackerThread extends Thread {
    
    private boolean terminate = false;
    
    private Map<TrainStop, Long> activeTrainStops = new HashMap<TrainStop, Long>();
    private Map<TrainStop, Long> pendingStops = new HashMap<TrainStop, Long>();
    
    @Override
    public void run()
    {
        List<TrainStop> flagged = new LinkedList<TrainStop>();
        
        while (!terminate) {
            for (TrainStop stop : getTrainStops()) {
                if ((System.currentTimeMillis() - getLastUpdateForStop(stop)) > 1800000) {
                    flagged.add(stop);
                }
            }
            
            for (TrainStop stop : flagged) {
                unregisterTrainStop(stop);
                stop = null;
            }
            
            flagged.clear();
        }
        
        flagged = null;
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
        activeTrainStops.put(stop, System.currentTimeMillis());
    }
    
    public void unregisterTrainStop(TrainStop stop)
    {
        activeTrainStops.remove(stop);
    }
    
    public void updateStopTime(TrainStop stop)
    {
        activeTrainStops.put(stop, System.currentTimeMillis());
    }
    
    private List<TrainStop> getTrainStops()
    {
        LinkedList<TrainStop> l = new LinkedList<TrainStop>();
        
        for (Map.Entry<TrainStop, Long> entry : activeTrainStops.entrySet()) {
            l.add(entry.getKey());
        }
        
        return l;
    }
    
    private long getLastUpdateForStop(TrainStop stop)
    {
        return activeTrainStops.get(stop);
    }
}

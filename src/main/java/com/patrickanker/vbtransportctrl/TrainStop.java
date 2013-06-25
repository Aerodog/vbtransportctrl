/*
 * TrainStop.java
 *
 * Project: vbtransportctrl
 *
 * Copyright (C) Patrick Anker 2013. All rights reserved.
 * 
 * vbtransportctrl by Patrick Anker is licensed under a Creative Commons 
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 *
 */

/*
 *
 * #railstop
 * time
 * DIRECTION
 * y/n
 * 
 */

package com.patrickanker.vbtransportctrl;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class TrainStop {
    
    private final Location location;
    private final int timeout;
    private final String direction;
    private final boolean spawnMinecartOnSignPower;
    
    public TrainStop(Location loc)
    {
        location = loc.getBlock().getLocation();
        Sign _controlSign = (Sign) location.getBlock().getWorld().getBlockAt(location.getBlock().getX(), location.getBlock().getY(), location.getBlock().getZ() - 2);
        
        Integer _time = Integer.parseInt(_controlSign.getLine(1));
        timeout = _time;
        
        String dir = _controlSign.getLine(2);
        direction = dir.toLowerCase();
        
        String foo = _controlSign.getLine(3);
        
        if (foo != null) {
            if (foo.equalsIgnoreCase("y")) {
                spawnMinecartOnSignPower = true;
            } else if (foo.equalsIgnoreCase("n")) {
                spawnMinecartOnSignPower = false;
            } else {
                spawnMinecartOnSignPower = false;
            }
        } else {
            spawnMinecartOnSignPower = false;
        }
    }
    
    public Location getTrackLocation()
    {
        return location;
    }
    
    public Location getControlSignLocation()
    {
        return location.getBlock().getWorld().getBlockAt(location.getBlock().getX(), location.getBlock().getY(), location.getBlock().getZ() - 2).getLocation();
    }
    
    public int getTimeout()
    {
        return timeout;
    }
    
    public String getDirection()
    {
        return direction;
    }
    
    public boolean spawnMinecartOnSignPower()
    {
        return spawnMinecartOnSignPower;
    }
    
    public static boolean isStop(Location loc)
    {
        World w = loc.getWorld();
        Block b1 = loc.getBlock();
        
        if (b1.getTypeId() != 66)
            return false;
        
        if (w.getBlockAt(b1.getX(), b1.getY(), b1.getZ() - 2).getTypeId() != 63)
            return false;
        
        Sign controlSign = (Sign) w.getBlockAt(b1.getX(), b1.getY(), b1.getZ() - 2);
        
        if (!controlSign.getLine(0).equalsIgnoreCase("#railstop"))
            return false;
        
        try {
            Integer _time = Integer.parseInt(controlSign.getLine(1));
        } catch (Throwable t){
            return false;
        }
        
        String dir = controlSign.getLine(2);
        
        if (dir != null) {
            if (!dir.equalsIgnoreCase("north") && !dir.equalsIgnoreCase("east") && !dir.equalsIgnoreCase("south") && !dir.equalsIgnoreCase("west"))
                return false;
        } else {
            return false;
        }
        
        return true;
    }
}

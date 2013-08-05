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
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class TrainStop {
    
    private final Location location;
    private final int timeout;
    private final String direction;
    private final boolean spawnMinecartOnSignPower;
    
    private Minecart waitingCart;
    
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
    
    public boolean spawnMinecartOnSignPower()
    {
        return spawnMinecartOnSignPower;
    }
    
    public boolean hasWaitingCart()
    {
        return waitingCart != null;
    }
    
    public Minecart getWaitingCart()
    {
        return waitingCart;
    }
    
    public void setWaitingCart(Minecart cart)
    {
        if (cart == null && hasWaitingCart())
            waitingCart = cart;
        
        if (!hasWaitingCart())
            waitingCart = cart;
    }
    
    public void deploy()
    {
        if (hasWaitingCart()) {
            if (direction.equals("north")) {
                waitingCart.setVelocity(new Vector(0, -waitingCart.getMaxSpeed(), 0));
                setWaitingCart(null);
            } else if (direction.equals("east")) {
                waitingCart.setVelocity(new Vector(waitingCart.getMaxSpeed(), 0, 0));
                setWaitingCart(null);
            } else if (direction.equals("south")) {
                waitingCart.setVelocity(new Vector(0, waitingCart.getMaxSpeed(), 0));
                setWaitingCart(null);
            } else {
                waitingCart.setVelocity(new Vector(-waitingCart.getMaxSpeed(), 0, 0));
                setWaitingCart(null);
            }
        }
    }
    
    public void halt(Minecart cart)
    {
        if (!hasWaitingCart()) {
            setWaitingCart(cart);
            cart.setVelocity(new Vector(0, 0, 0));
        }
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

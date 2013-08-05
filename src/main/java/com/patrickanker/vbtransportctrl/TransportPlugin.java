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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class TransportPlugin extends JavaPlugin implements Listener {

    private static TransportPlugin singleton;
    
    private TrainStopTrackerThread trainStopTrackerThread = new TrainStopTrackerThread();
    private MinecartTrackerThread minecartTrackerThread = new MinecartTrackerThread();
    
    private boolean configSpitDebugMessages = false;
    
    @Override
    public void onEnable()
    {
        singleton = this;
        
        try {
            readConfig();
        } catch (Throwable t) {
            Bukkit.getLogger().severe("Could not read config file: " + t.getMessage());
        }
        
        pluginStartupPrintout();
        
        for (World world : Bukkit.getWorlds()) {
            List<Entity> _entities = world.getEntities();
            
            for (Entity _entity : _entities) {
                if ((_entity instanceof Minecart) && (_entity.getPassenger() != null) && (_entity.getPassenger() instanceof Player))
                    minecartTrackerThread.reload((Minecart) _entity);
            }
            
            DEBUG("Loaded any active minecarts to Minecart Tracker Thread in world: " + world.getName());
        }
        
        trainStopTrackerThread.start();
        minecartTrackerThread.start();
        DEBUG("Started processes");
        
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        DEBUG("Events registered");
        
        DEBUG("Finished loading");
    }
    
    @Override
    public void onDisable()
    {
        trainStopTrackerThread.terminate();
        minecartTrackerThread.terminate();
        
        DEBUG("Terminated processes");
        
        try {
            writeConfig();
        } catch (Throwable t) {
            Bukkit.getLogger().severe("Could not write config file: " + t.getMessage());
        }
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void onVehicleCreate(VehicleCreateEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        
        if (vehicle instanceof Minecart) {
            Minecart cart = (Minecart) vehicle;
            
            if (TrainStop.isStop(cart.getLocation())) {
                DEBUG("Stop detected");
                
                if (trainStopTrackerThread.isTrainStopRegistered(cart.getLocation())) {
                    TrainStop stop = trainStopTrackerThread.getTrainStopForLocation(cart.getLocation());
                    trainStopTrackerThread.updateStopTime(stop);
                } else {
                    TrainStop stop = new TrainStop(cart.getLocation());
                    trainStopTrackerThread.registerTrainStop(stop);
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void onVehicleEnter(VehicleEnterEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        
        if ((vehicle instanceof Minecart) && (event.getEntered() instanceof Player)) {
            Minecart cart = (Minecart) vehicle;
            minecartTrackerThread.activateCart(cart);
            DEBUG("Activated cart");
            
            if (trainStopTrackerThread.isTrainStopRegistered(cart.getLocation())) {
                TrainStop stop = trainStopTrackerThread.getTrainStopForLocation(cart.getLocation());
                stop.deploy();
                
                if (trainStopTrackerThread.isPendingStop(stop))
                    trainStopTrackerThread.removePendingStop(stop);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void onVehicleExit(VehicleExitEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        
        if ((vehicle instanceof Minecart) && (event.getExited() instanceof Player)) {
            Minecart cart = (Minecart) vehicle;
            minecartTrackerThread.deactivateCart(cart);
            DEBUG("Deactivated cart");
        }
    }
    
    public static MinecartTrackerThread getMinecartManager()
    {
        return singleton.minecartTrackerThread;
    }
    
    public static TrainStopTrackerThread getTrainStopManager()
    {
        return singleton.trainStopTrackerThread;
    }
    
    public static void DEBUG(String message)
    {
        if (singleton.configSpitDebugMessages)
            Bukkit.getLogger().info("[VBTRNCTRL:Debug] " + message);
    }
    
    private static void readConfig() throws IOException, InvalidConfigurationException
    {
        File configFile = new File(singleton.getDataFolder().getAbsolutePath() + "/config.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        } else {
            config.load(configFile);
            singleton.configSpitDebugMessages = config.getBoolean("debugmessages");
            
            DEBUG("Loaded config");
        }
    }
    
    private static void writeConfig() throws IOException
    {
        File configFile = new File(singleton.getDataFolder().getAbsolutePath() + "/config.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            
            config.set("debugmessages", singleton.configSpitDebugMessages);
            config.save(configFile);
            DEBUG("Saved config");
        } else {
            config.set("debugmessages", singleton.configSpitDebugMessages);
            config.save(configFile);
            DEBUG("Saved config");
        }
    }
    
    private static void pluginStartupPrintout()
    {
        DEBUG("==========================================");
        DEBUG("Running VB Transport Control version " + singleton.getDescription().getVersion());
        
        try {
            DEBUG("MD5: " + getMD5Hash());
        } catch (Throwable t) {
            Bukkit.getLogger().severe("Could not generate MD5 hash for plugin file");
        }
        
        DEBUG("");
        DEBUG("==========================================");
    }
    
    private static String getMD5Hash() throws Exception
    {
        InputStream _inputStream = new FileInputStream(singleton.getFile());
        
        byte[] buffer = new byte[1024];
        MessageDigest md = MessageDigest.getInstance("MD5");
        int numRead;
        
        while ((numRead = _inputStream.read(buffer)) != -1) {
            if (numRead > 0) {
                md.update(buffer, 0, numRead);
            }
        }
        
        _inputStream.close();
        byte[] input = md.digest();
        
        String res = "";
        
        for (int i = 0; i < input.length; ++i) {
            res += Integer.toString((input[i] & 0xff) + 0x100, 16).substring(1);
        }
        
        return res;
    }
}

/*
 * TransportPlugin.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    
    private List<TrainStop> registeredTrainStops = new LinkedList<TrainStop>();
    private Map<TrainStop, Long> lastStopUpdateTime = new HashMap<TrainStop, Long>();
    
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
        int numRead = 0;
        
        while (numRead != -1) {
            numRead = _inputStream.read(buffer);
            
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

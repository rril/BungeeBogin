package org.rril.bungeelogin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.rril.bungeelogin.Commands.CommandBPortals;
import org.rril.bungeelogin.Tasks.SaveTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.rril.bungeelogin.listeners.CommandManager;
import org.rril.bungeelogin.listeners.EventsManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.rril.bungeelogin.listeners.vAuthListen;

/**
 * Main class of bungeelogin plugin
 *
 * @author Stakzz
 * @version 0.9.0
 */
public class bungeelogin extends JavaPlugin implements Listener {

    public static Logger logger = Bukkit.getLogger();
    public Map<String, String> portalData = new HashMap<String, String>();
    public WorldEditPlugin worldEdit = null;
    public YamlConfiguration configFile = null;
    public YamlConfiguration portalsFile = null;

    /**
     * Declaration of the current plugin : bungeelogin
     */
    public static bungeelogin plugin;
    /**
     * Database connector of the plugin
     */
    public static MysqlConnector databaseConnection;
    /**
     * vAuth Database connector of the plugin
     */
    public static vAuthListen vAuthDatabaseConnection;
    /**
     * Server logger
     */
    //public static Logger logger;
    /**
     * Server pluginmanager
     */
    public static PluginManager pluginManager;
    /**
     * HashMap for players session
     */
    public static final HashMap<String, String> sessions = new HashMap<String, String>();

    /**
     * vAuth DB used
     */
    public static boolean vAuth;

    /**
     * Default prompt for bungeelogin plugin messages
     */
    public static final String PROMPT = ChatColor.WHITE + "[" + ChatColor.AQUA + "bungeelogin" + ChatColor.WHITE + "] ";

    public void onEnable() {
        sessions.clear();
        plugin = this;
        pluginManager = getServer().getPluginManager();
        logger = getLogger();
        loadConfigurationFiles();

        getCommand("BPortals").setExecutor(new CommandBPortals(this));
        this.logger.log(Level.INFO, " Commands registered!");
        getServer().getPluginManager().registerEvents(new EventsManager(this), this);
        this.logger.log(Level.INFO, " Events registered!");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.logger.log(Level.INFO, " Plugin channel registered!");
        loadPortalsData();
        Integer interval = this.configFile.getInt("SaveTask.Interval") * 20;
        new SaveTask(this).runTaskTimer(this, interval, interval);
        this.logger.log(Level.INFO, " Save task started!");

        vAuth = configFile.getBoolean("vAuth", false);
        String host = this.configFile.getString("DatabaseHost");
        int port = this.configFile.getInt("DatabasePort");
        String database = this.configFile.getString("DatabaseName");
        String user = this.configFile.getString("DatabaseUsername");
        String password = this.configFile.getString("DatabasePassword");

        try {
            if (vAuth) {
                bungeelogin.vAuthDatabaseConnection = new vAuthListen(plugin);
            } else {
                databaseConnection = new MysqlConnector(host, port, database, user, password);
            }
            //pluginManager.registerEvents(new EventsManager(this), this);
            getCommand("bungeelogin").setExecutor(new CommandManager());
            getCommand("register").setExecutor(new CommandManager());
            getCommand("login").setExecutor(new CommandManager());
            getCommand("logout").setExecutor(new CommandManager());
            getCommand("changepw").setExecutor(new CommandManager());
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "ClassNotFound Exception - " + e.toString());
            pluginManager.disablePlugin(pluginManager.getPlugin(getName()));
            databaseConnection = null;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception - " + e.toString());
            pluginManager.disablePlugin(pluginManager.getPlugin(getName()));
            databaseConnection = null;
        }

        Long time = System.currentTimeMillis();
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getPluginLoader().disablePlugin(this);
            throw new NullPointerException(" WorldEdit not found, disabling...");
        }
        this.worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        this.logger.log(Level.INFO, " Version " + getDescription().getVersion() + " has been enabled. (" + (System.currentTimeMillis() - time) + "ms)");

    }

    public void loadPortalsData() {
        try {
            Long time = System.currentTimeMillis();
            for (String key : this.portalsFile.getKeys(false)) {
                String value = this.portalsFile.getString(key);
                this.portalData.put(key, value);
            }
            this.logger.log(Level.INFO, " Portal data loaded! (" + (System.currentTimeMillis() - time) + "ms)");
        } catch (NullPointerException e) {
        }
    }

    public void onDisable() {
        Long time = System.currentTimeMillis();
        savePortalsData();
        this.logger.log(Level.INFO, " Version " + getDescription().getVersion() + " has been disabled. (" + (System.currentTimeMillis() - time) + "ms)");

        if (databaseConnection != null) {
            try {
                databaseConnection.closeConnection();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Exception - " + e.toString());
            }
        }
    }

    /**
     * Player is registered ? true/false
     *
     * @param player
     * @return boolean True if player is already registered
     */
    public static boolean isRegistered(Player player) {
        return isRegistered(player.getUniqueId().toString());
    }

    /**
     * Username is registered ? True/false
     *
     * @param uuid
     * @return boolean True if name is already registered
     */
    public static boolean isRegistered(String uuid) {
        if (vAuth) {
            try {
                return !vAuthDatabaseConnection.isRegister(uuid);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "vAuth Exception - " + e.toString());
            }
        }
        try {
            String query = "SELECT COUNT( * ) FROM users WHERE username =  '" + uuid + "';";
            ResultSet result = databaseConnection.executeQuery(query);
            result.first();

            if (result.getInt(1) == 0) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception - " + e.toString());
            pluginManager.disablePlugin(pluginManager.getPlugin(plugin.getName()));
            databaseConnection = null;
            return false;
        }
    }

    /**
     * Player is logged ? true/false
     *
     * @param player
     * @return boolean True if player is already logged
     */
    public static boolean isLogged(Player player) {
        if (sessions.get(player.getUniqueId().toString()) != null) {
            return true;
        }
        return false;
    }

    private void createConfigurationFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfigurationFiles() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            createConfigurationFile(getResource("config.yml"), configFile);
            this.logger.log(Level.INFO, " Configuration file config.yml created!");
        }
        this.configFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        this.logger.log(Level.INFO, " Configuration file config.yml loaded!");
        File portalsFile = new File(getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            portalsFile.getParentFile().mkdirs();
            createConfigurationFile(getResource("portals.yml"), portalsFile);
            this.logger.log(Level.INFO, " Configuration file portals.yml created!");
        }
        this.portalsFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "portals.yml"));
        this.logger.log(Level.INFO, " Configuration file portals.yml loaded!");
    }

    public void savePortalsData() {
        Long time = System.currentTimeMillis();
        for (Map.Entry<String, String> entry : this.portalData.entrySet()) {
            this.portalsFile.set(entry.getKey(), entry.getValue());
        }
        try {
            this.portalsFile.save(new File(getDataFolder(), "portals.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.log(Level.INFO, " Portal data saved! (" + (System.currentTimeMillis() - time) + "ms)");
    }
}

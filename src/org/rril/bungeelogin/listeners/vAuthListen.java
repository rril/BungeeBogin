/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rril.bungeelogin.listeners;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.rril.bungeelogin.bungeelogin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 *
 * @author rril
 */
public final class vAuthListen {

    /**
     * Where to teleport our players to when they login
     */
    //public String loginLocation = "0>>64>>0>>world";
    /**
     * Our Config file's location
     */
    //public File ConfigFile;
    /**
     * Our Config controller
     */
    //public FileConfiguration Config;
    /**
     * Our Passwords controller
     */
    public FileConfiguration UserPass;
    /**
     * Our Passwords file's location
     */
    public File UserPassFile;
    /**
     * Our Configuration for the converter
     */
    //FileConfiguration Convert;

    private bungeelogin plugin;

    /**
     * Plugin enable
     */
    
    public vAuthListen(bungeelogin plugin) {
        this.plugin = plugin;
        //loginLocation = "0>>64>>0>>world";
        //ConfigFile = new File(this.plugin.getDataFolder(), "config.yml");
        UserPassFile = new File(this.plugin.getDataFolder(), "passwords.yml");
        //Config = YamlConfiguration.loadConfiguration(ConfigFile);
        UserPass = YamlConfiguration.loadConfiguration(UserPassFile);
         plugin.getLogger().info("vAuth in used.");
    }

    public boolean loginCheck(Player player, String password) {
        String decryptedPassword = null;
        try {
            return comparePasswordToHash(password, UserPass.getString(player.getUniqueId().toString()));
        } catch (Exception e) {
            decryptedPassword = Base64Coder.decodeString(UserPass.getString(player.getUniqueId().toString()));
        }
        if (password.equals(decryptedPassword)) {
            register(player, decryptedPassword, decryptedPassword);
            return true;
        }
        return false;
    }

    public boolean register(Player player, String password, String confirmedPassword) {
        String encryptedText = null;
        if (password.equals(confirmedPassword)) {
            encryptedText = hashAndEncode(password);
            if (encryptedText == null) {
                plugin.getLogger().warning(player.getName() + ChatColor.RED + " Failed to encrypt password!");
                return false;
            }
            UserPass.set(player.getUniqueId().toString(), encryptedText);
            try {
                UserPass.save(UserPassFile);
            } catch (IOException e) {
                plugin.getLogger().warning(player.getName() + ChatColor.RED + " Failed to save password! " + e.toString());                
                return false;
            }
            return true;
        }
                plugin.getLogger().warning(player.getName() + ChatColor.RED + " STOP the bla bla.");                
        return false;
    }

    public boolean remove(Player player) {
        if(this.isRegister(player)){
            UserPass.set(player.getUniqueId().toString(), null);
            try {
                UserPass.save(UserPassFile);
            } catch (IOException e) {
                plugin.getLogger().warning(player.getName() + ChatColor.RED + " Failed to save password! " + e.toString());                
                return false;
            }
            return true;
        }
        return false;        
    }

    public boolean comparePasswordToHash(String password, String encodedHash) {
        byte[] combinedHash = Base64Coder.decode(encodedHash);
        byte[] salt = Arrays.copyOfRange(combinedHash, 0, SALT_SIZE);
        byte[] saltedHash = Arrays.copyOfRange(combinedHash, SALT_SIZE, combinedHash.length);

        byte[] testHash = hashWithSalt(password, salt);

        return Arrays.equals(saltedHash, testHash);
    }
    private final int SALT_SIZE = 20;

    private static byte[] hashWithSalt(String password, byte[] salt) {
        MessageDigest hash;

        try {
            hash = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        hash.update(password.getBytes());
        hash.update(salt);
        return hash.digest();
    }

    public String hashAndEncode(String password) {
        byte[] salt = generateSalt();
        byte[] saltedHash = hashWithSalt(password, salt);

        byte[] combinedHash = new byte[salt.length + saltedHash.length];
        System.arraycopy(salt, 0, combinedHash, 0, salt.length);
        System.arraycopy(saltedHash, 0, combinedHash, salt.length, saltedHash.length);
        return new String(Base64Coder.encode(combinedHash));
    }

    private byte[] generateSalt() {
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];

        rng.nextBytes(salt);

        return salt;
    }
    
    public boolean isRegister(Player player){
        return UserPass.getString(player.getUniqueId().toString(),"").equalsIgnoreCase("");
    }
    
    public boolean isRegister(String uuid){
        return UserPass.getString(uuid.toString(),"").equalsIgnoreCase("");
    }
    
    public boolean isRegister(UUID uuid){
        return UserPass.getString(uuid.toString(),"").equalsIgnoreCase("");
    }
}

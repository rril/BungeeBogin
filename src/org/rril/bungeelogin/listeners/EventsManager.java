package org.rril.bungeelogin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.rril.bungeelogin.bungeelogin;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Listener class of bungeelogin plugin
 *
 * @author Stakzz
 * @version 0.9.0
 */
public class EventsManager implements Listener {

    private bungeelogin plugin;
    private Map<String, Boolean> statusData = new HashMap<String, Boolean>();

    public EventsManager(bungeelogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws IOException {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!this.statusData.containsKey(playerName)) {
            this.statusData.put(playerName, false);
        }
        Block block = player.getWorld().getBlockAt(player.getLocation());
        String data = block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ());
        if (plugin.portalData.containsKey(data)) {
            if (!this.statusData.get(playerName)) {
                this.statusData.put(playerName, true);
                String destination = plugin.portalData.get(data);
                if (player.hasPermission("BungeePortals.portal." + destination) || player.hasPermission("BungeePortals.portal.*")) {
                    if (bungeelogin.isLogged(event.getPlayer())) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);
                        dos.writeUTF("Connect");
                        dos.writeUTF(destination);
                        player.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
                        baos.close();
                        dos.close();
                        bungeelogin.sessions.remove(player.getUniqueId().toString());
                    } else {
                        if (!bungeelogin.isRegistered(player)) {
                            player.sendMessage(plugin.configFile.getString("PleaseRegisterToUse").replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
                        } else {
                            player.sendMessage(plugin.configFile.getString("PleaseLoginToUse").replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
                        }
                    }

                } else {
                    player.sendMessage(plugin.configFile.getString("NoPortalPermissionMessage").replace("{destination}", destination).replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
                }
            }
        } else {
            if (this.statusData.get(playerName)) {
                this.statusData.put(playerName, false);
            }
        }
    }

    /**
     * When a player joins the server
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (bungeelogin.isLogged(event.getPlayer())) {
            bungeelogin.sessions.remove(event.getPlayer().getUniqueId().toString());
        }
        if (!bungeelogin.isRegistered(event.getPlayer())) {
            event.getPlayer().sendMessage("Welcome " + event.getPlayer().getName() + " on our server!");
            event.getPlayer().sendMessage("You aren't registered yet. Please register with :");
            event.getPlayer().sendMessage(ChatColor.GOLD + "/register <password> <password>");
        } else {
            event.getPlayer().sendMessage("Welcome back " + event.getPlayer().getName() + " on our server!");
            event.getPlayer().sendMessage("You are already registered. Please login with :");
            event.getPlayer().sendMessage(ChatColor.GOLD + "/login <password>");
        }
    }

    /**
     * When a player moves : cancel moves
     *
     * @param event
     */
    /*
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerMove(PlayerMoveEvent event) {
     if (!bungeelogin.isLogged(event.getPlayer())) {
     Location from = event.getFrom();
     Location to = event.getTo();
     if ((from.getBlockX() != to.getBlockX()) || (from.getBlockZ() != to.getBlockZ())
     || (from.getBlockY() != to.getBlockY())) {
     event.setTo(from);
     event.getPlayer().sendMessage(bungeelogin.PROMPT + ChatColor.RED + "You must be logged in to move");
     }
     }
     }*/
    /**
     * When a player try to talk in chat : cancel chat
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!bungeelogin.isLogged(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(bungeelogin.PROMPT + ChatColor.RED + "You must be logged in to chat");
        }
    }

    /**
     * When a player try to enter commands : just allow /login and /register
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!bungeelogin.isLogged(event.getPlayer())) {
            String message = event.getMessage().toLowerCase();
            if (!(message.startsWith("/login ")) && !(message.startsWith("/register ")) && !(message.startsWith("/r "))
                    && !(message.startsWith("/l "))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(bungeelogin.PROMPT + ChatColor.RED + "You must be logged in to enter commands");
            }
        }
    }

    /**
     * When a player try to interact why entities : cancel interactions
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!bungeelogin.isLogged(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(bungeelogin.PROMPT + ChatColor.RED + "You must be logged in to interract with world");
        }
    }

    /**
     * When a player try to drop something : cancel drop
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!bungeelogin.isLogged(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(bungeelogin.PROMPT + ChatColor.RED + "You must be logged in to drop items");
        }
    }

    /**
     * When a player leave : remove him from sessions
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (bungeelogin.isLogged(event.getPlayer())) {
            bungeelogin.sessions.remove(event.getPlayer().getUniqueId());
        }
    }
}

package org.wardenBlocker.wardenBlocker;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WardenBlocker extends JavaPlugin implements Listener {

    private final Set<UUID> blockedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> activeWardens = ConcurrentHashMap.newKeySet();

    private int detectionRadius;
    private int checkInterval;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        Bukkit.getPluginManager().registerEvents(new WardenCommandBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new WardenTeleportBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        // Scan for loaded wardens
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntitiesByClass(Warden.class)) {
                if (entity.isValid() && entity.isPersistent() && entity.getLocation().getChunk().isLoaded()) {
                    activeWardens.add(entity.getUniqueId());
                }
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, this::updateBlockedPlayers, 0L, checkInterval);
        getLogger().info("WardenBlocker enabled! Detection radius: " + detectionRadius + ", check interval: " + checkInterval + " ticks.");
    }

    public void loadSettings() {
        detectionRadius = getConfig().getInt("detection-radius", 30);
        checkInterval = getConfig().getInt("check-interval", 40);
    }

    private void updateBlockedPlayers() {
        blockedPlayers.clear();

        if (activeWardens.isEmpty()) return;

        // Get only *loaded* wardens
        List<LivingEntity> loadedWardens = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getEntitiesByClass(Warden.class)) {
                if (entity.isValid() && entity.getLocation().getChunk().isLoaded()) {
                    loadedWardens.add(entity);
                }
            }
        }

        if (loadedWardens.isEmpty()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (LivingEntity warden : loadedWardens) {
                if (warden.getLocation().distanceSquared(player.getLocation()) <= detectionRadius * detectionRadius) {
                    blockedPlayers.add(player.getUniqueId());
                    break;
                }
            }
        }
    }

    public boolean isBlocked(Player player) {
        return blockedPlayers.contains(player.getUniqueId());
    }

    // Track wardens when they spawn
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            activeWardens.add(event.getEntity().getUniqueId());
        }
    }

    // Remove wardens from tracker when they die
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            activeWardens.remove(event.getEntity().getUniqueId());
        }
    }

    // Remove wardens from tracker when chunks unload
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() == EntityType.WARDEN) {
                activeWardens.remove(entity.getUniqueId());
            }
        }
    }

    // Add wardens back to tracker when chunks load (if valid)
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() == EntityType.WARDEN) {
                activeWardens.add(entity.getUniqueId());
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("wardenblocker.reload")) {
                sender.sendMessage("§cYou do not have permission to do that.");
                return true;
            }

            reloadConfig();
            loadSettings();
            sender.sendMessage("§aWardenBlocker config reloaded.");
            return true;
        }

        sender.sendMessage("§eUsage: /wardenblocker reload");
        return true;
    }
}

package org.wardenBlocker.wardenBlocker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WardenBlocker extends JavaPlugin {

    private final Set<UUID> blockedPlayers = new HashSet<>();
    private int detectionRadius;
    private int checkInterval;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        Bukkit.getPluginManager().registerEvents(new WardenCommandBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new WardenTeleportBlocker(this), this);

        Bukkit.getScheduler().runTaskTimer(this, this::updateBlockedPlayers, 0L, checkInterval);

        getLogger().info("WardenBlocker enabled! Detection radius: " + detectionRadius + ", check interval: " + checkInterval + " ticks.");
    }

    public void loadSettings() {
        detectionRadius = getConfig().getInt("detection-radius", 30);
        checkInterval = getConfig().getInt("check-interval", 40);
    }

    private void updateBlockedPlayers() {
        blockedPlayers.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean nearWarden = player.getNearbyEntities(detectionRadius, detectionRadius, detectionRadius)
                    .stream().anyMatch(e -> e.getType() == EntityType.WARDEN);
            if (nearWarden) {
                blockedPlayers.add(player.getUniqueId());
            }
        }
    }

    public boolean isBlocked(Player player) {
        return blockedPlayers.contains(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

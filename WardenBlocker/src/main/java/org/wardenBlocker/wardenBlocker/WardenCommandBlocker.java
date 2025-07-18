package org.wardenBlocker.wardenBlocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class WardenCommandBlocker implements Listener {

    private final WardenBlocker plugin;

    public WardenCommandBlocker(WardenBlocker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isBlocked(player)) return;

        String[] parts = event.getMessage().toLowerCase().trim().split("\\s+");
        if (parts.length == 0) return;

        String command = parts[0]; // includes leading "/"
        List<String> blocked = plugin.getConfig().getStringList("blocked-commands");

        String cleanCommand = command.startsWith("/") ? command.substring(1) : command;

        for (String blockedCommand : blocked) {
            String blockedClean = blockedCommand.startsWith("/") ? blockedCommand.substring(1) : blockedCommand;
            if (cleanCommand.startsWith(blockedClean)) {
                event.setCancelled(true);
                String message = plugin.getConfig().getString("block-message", "&cTeleport blocked!");
                player.sendMessage(message.replace("&", "§"));
                break;
            }
        }
    }
}

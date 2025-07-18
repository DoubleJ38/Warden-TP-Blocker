package org.wardenBlocker.wardenBlocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class WardenTeleportBlocker implements Listener {

    private final WardenBlocker plugin;

    public WardenTeleportBlocker(WardenBlocker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!plugin.isBlocked(player)) return;

        TeleportCause cause = event.getCause();

        // Allow all natural teleport causes
        switch (cause) {
            case CHORUS_FRUIT:
            case ENDER_PEARL:
            case NETHER_PORTAL:
            case END_PORTAL:
            case SPECTATE:
                return;
        }

        // Block command and plugin-based teleports
        event.setCancelled(true);
        String message = plugin.getConfig().getString("block-message", "&cTeleport blocked!");
        player.sendMessage(message.replace("&", "§"));
    }
}

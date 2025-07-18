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

        if (plugin.isBlocked(player)) return;

        // Optionally allow some causes like END_PORTAL or SPECTATE
        TeleportCause cause = event.getCause();
        if (cause == TeleportCause.CHORUS_FRUIT || cause == TeleportCause.NETHER_PORTAL || cause == TeleportCause.END_PORTAL) {
            return; // allow natural/mobility-based teleporting if you want
        }

        event.setCancelled(true);
        String message = plugin.getConfig().getString("block-message", "&cTeleport blocked!");
        player.sendMessage(message.replace("&", "§"));
    }
}

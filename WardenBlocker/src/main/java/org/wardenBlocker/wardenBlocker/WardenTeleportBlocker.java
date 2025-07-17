package org.wardenBlocker.wardenBlocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WardenTeleportBlocker implements Listener {

    private final WardenBlocker plugin;

    public WardenTeleportBlocker(WardenBlocker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (plugin.isBlocked(player)) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("block-message", "&cTeleport blocked!");
            player.sendMessage(message.replace("&", "§"));
        }
    }
}

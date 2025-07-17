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

        String msg = event.getMessage().toLowerCase().split(" ")[0]; // command part only
        List<String> blocked = plugin.getConfig().getStringList("blocked-commands");

        if (blocked.stream().anyMatch(msg::startsWith)) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("block-message", "&cTeleport blocked!");
            player.sendMessage(message.replace("&", "§"));
        }
    }
}

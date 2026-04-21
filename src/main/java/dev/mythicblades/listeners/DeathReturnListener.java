package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathReturnListener implements Listener {

    private final MythicBladesPlugin plugin;
    // Store swords to give back on respawn
    private final Map<UUID, java.util.List<SwordType>> pendingReturn = new HashMap<>();

    public DeathReturnListener(MythicBladesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        java.util.List<SwordType> owned = new java.util.ArrayList<>();

        // Remove all mythic swords from drops — they return to owner
        event.getDrops().removeIf(item -> {
            SwordType type = plugin.getSwordManager().getSwordType(item);
            if (type != null && plugin.getOwnershipManager().isOwnedBy(type, player)) {
                owned.add(type);
                return true; // remove from drops
            }
            return false;
        });

        if (!owned.isEmpty()) {
            pendingReturn.put(player.getUniqueId(), owned);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        java.util.List<SwordType> swords = pendingReturn.remove(player.getUniqueId());
        if (swords == null) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (SwordType type : swords) {
                ItemStack sword = plugin.getSwordManager().createSword(type);
                // Re-apply ownership tag
                plugin.getSwordManager().setOwner(sword, player.getUniqueId().toString());
                // Re-apply awakened state if applicable
                if (plugin.getAwakeningManager().isAwakened(player.getUniqueId(), type)) {
                    plugin.getSwordManager().setAwakened(sword);
                }
                player.getInventory().addItem(sword);
            }
            player.sendMessage("§6[MythicBlades] §7Your blade(s) have returned to you.");
        }, 5L);
    }
}

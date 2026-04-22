package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeathReturnListener implements Listener {

    private final MythicBladesPlugin plugin;
    private final Map<UUID, List<SwordType>> pendingReturn = new HashMap<>();

    public DeathReturnListener(MythicBladesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<SwordType> owned = new ArrayList<>();

        event.getDrops().removeIf(item -> {
            SwordType type = plugin.getSwordManager().getSwordType(item);
            if (type != null && plugin.getOwnershipManager().isOwnedBy(type, player)) {
                owned.add(type);
                return true;
            }
            return false;
        });

        if (!owned.isEmpty()) pendingReturn.put(player.getUniqueId(), owned);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<SwordType> swords = pendingReturn.remove(player.getUniqueId());
        if (swords == null) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (SwordType type : swords) {
                ItemStack sword = plugin.getSwordManager().createSword(type);
                plugin.getSwordManager().setOwner(sword, player.getUniqueId().toString());
                if (plugin.getAwakeningManager().isAwakened(player.getUniqueId(), type))
                    plugin.getSwordManager().setAwakened(sword);
                player.getInventory().addItem(sword);
            }
            player.sendMessage("§6[MythicBlades] §7Your blade(s) have returned to you.");
        }, 5L);
    }
}

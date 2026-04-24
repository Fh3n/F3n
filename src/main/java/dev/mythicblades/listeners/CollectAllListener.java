package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class CollectAllListener implements Listener {

    private final MythicBladesPlugin plugin;

    public CollectAllListener(MythicBladesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (!plugin.getSwordManager().isMythicSword(item)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getOwnershipManager().ownsAllSeven(player) &&
                !plugin.getBuffManager().hasBuff(player))
                plugin.getBuffManager().applyCollectAllBuff(player);
        }, 5L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!plugin.getSwordManager().isMythicSword(event.getCurrentItem())) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getOwnershipManager().ownsAllSeven(player) &&
                !plugin.getBuffManager().hasBuff(player))
                plugin.getBuffManager().applyCollectAllBuff(player);
        }, 5L);
    }
}

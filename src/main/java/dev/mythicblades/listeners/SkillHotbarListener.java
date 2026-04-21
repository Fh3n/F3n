package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SkillHotbarListener implements Listener {

    private final MythicBladesPlugin plugin;

    public SkillHotbarListener(MythicBladesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        SwordType type = plugin.getSwordManager().getSwordType(newItem);

        if (type == null) {
            plugin.getSkillHotbarManager().clearSkillBar(player);
        } else {
            plugin.getSkillHotbarManager().showSkillBar(player, type);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSkillHotbarManager().clearSkillBar(event.getPlayer());
    }
}

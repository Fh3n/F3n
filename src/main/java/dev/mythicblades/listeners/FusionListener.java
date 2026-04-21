package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.swords.BladeOfThawSkills;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class FusionListener implements Listener {
    private final MythicBladesPlugin plugin;
    public FusionListener(MythicBladesPlugin plugin) { this.plugin = plugin; }

    // Fusion is triggered via /mb fuse command — no listener needed here
    // This listener handles Blade of Thaw resurrection trigger on fatal damage

    @EventHandler
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth > 0) return; // Not fatal

        // Check if holding Blade of Thaw and resurrection is armed
        ItemStack item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type != SwordType.BLADE_OF_THAW) return;

        if (!BladeOfThawSkills.isResurrectionReady(player.getUniqueId())) return;

        // CANCEL DEATH — trigger resurrection
        event.setCancelled(true);
        player.setHealth(1.0); // Temporary — resurrection sets to full
        BladeOfThawSkills.triggerResurrection(player, plugin);
    }
}

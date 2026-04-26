package dev.mythicblades.listeners;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import dev.mythicblades.swords.BladeOfThawSkills;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class FusionListener implements Listener {

    private final MythicBladesPlugin plugin;

    public FusionListener(MythicBladesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (BladeOfThawSkills.isInResurrection(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (player.getHealth() - event.getFinalDamage() > 0) return;

        var item = player.getInventory().getItemInMainHand();
        SwordType type = plugin.getSwordManager().getSwordType(item);
        if (type != SwordType.BLADE_OF_THAW) return;
        if (!BladeOfThawSkills.isResurrectionReady(player.getUniqueId())) return;

        event.setCancelled(true);
        player.setHealth(1.0);
        BladeOfThawSkills.triggerResurrection(player, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isInvulnerable()) {
            player.setInvulnerable(false);
        }
    }
}

package dev.mythicblades.managers;

import dev.mythicblades.MythicBladesPlugin;
import dev.mythicblades.SwordType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FusionManager {

    private final MythicBladesPlugin plugin;

    public FusionManager(MythicBladesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean attemptFusion(Player player) {
        AwakeningManager awakening = plugin.getAwakeningManager();
        OwnershipManager ownership = plugin.getOwnershipManager();
        SwordManager swordMgr     = plugin.getSwordManager();

        // Check ownership
        if (!ownership.isOwnedBy(SwordType.ENMA, player)) {
            player.sendMessage(Component.text("You do not own Enma.", NamedTextColor.RED));
            return false;
        }
        if (!ownership.isOwnedBy(SwordType.AME_NO_HABAKIRI, player)) {
            player.sendMessage(Component.text("You do not own Ame no Habakiri.", NamedTextColor.RED));
            return false;
        }

        // Check awakening
        if (!awakening.isAwakened(player.getUniqueId(), SwordType.ENMA)) {
            player.sendMessage(Component.text("Enma has not been Awakened yet. (" +
                awakening.getKillProgress(player.getUniqueId(), SwordType.ENMA) + "/500 kills)", NamedTextColor.RED));
            return false;
        }
        if (!awakening.isAwakened(player.getUniqueId(), SwordType.AME_NO_HABAKIRI)) {
            player.sendMessage(Component.text("Ame no Habakiri has not been Awakened yet. (" +
                awakening.getKillProgress(player.getUniqueId(), SwordType.AME_NO_HABAKIRI) + "/500 kills)", NamedTextColor.RED));
            return false;
        }

        // Check Kagura not already claimed
        if (ownership.isClaimed(SwordType.KAGURA_NO_TACHI)) {
            player.sendMessage(Component.text("Kagura no Tachi already exists in this world.", NamedTextColor.DARK_PURPLE));
            return false;
        }

        // Find and remove both swords from inventory
        boolean hasEnma = false, hasHabakiri = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;
            SwordType type = swordMgr.getSwordType(item);
            if (type == SwordType.ENMA) hasEnma = true;
            if (type == SwordType.AME_NO_HABAKIRI) hasHabakiri = true;
        }

        if (!hasEnma || !hasHabakiri) {
            player.sendMessage(Component.text("You must be holding both Enma and Ame no Habakiri in your inventory.", NamedTextColor.RED));
            return false;
        }

        // Begin fusion ritual
        performFusionRitual(player);
        return true;
    }

    private void performFusionRitual(Player player) {
        player.sendMessage(Component.text("§5The blades begin to resonate...", NamedTextColor.DARK_PURPLE));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                Location loc = player.getLocation().add(0, 1, 0);

                if (tick < 60) {
                    // Fire particles from below
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.5, 0.5, 0.5, 0.05);
                    // Holy light from above
                    player.getWorld().spawnParticle(Particle.END_ROD,
                        loc.clone().add(0, 2, 0), 8, 0.3, 0.3, 0.3, 0.02);
                    if (tick % 10 == 0) {
                        player.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, (float)(0.5 + tick * 0.01));
                    }
                } else if (tick == 60) {
                    // Fusion moment
                    player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3, 1, 1, 1, 0);
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 1, 1, 1, 0.1);
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.1);
                    player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1f, 1.5f);
                    player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);

                    // Remove both swords, give Kagura
                    SwordManager swordMgr = plugin.getSwordManager();
                    var inv = player.getInventory();
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack item = inv.getItem(i);
                        if (item == null) continue;
                        SwordType t = swordMgr.getSwordType(item);
                        if (t == SwordType.ENMA || t == SwordType.AME_NO_HABAKIRI) {
                            inv.setItem(i, null);
                        }
                    }

                    // Unclaim both
                    plugin.getOwnershipManager().unclaim(SwordType.ENMA);
                    plugin.getOwnershipManager().unclaim(SwordType.AME_NO_HABAKIRI);

                    // Create and give Kagura
                    ItemStack kagura = swordMgr.createSword(SwordType.KAGURA_NO_TACHI);
                    plugin.getOwnershipManager().claim(SwordType.KAGURA_NO_TACHI, player, kagura);
                    player.getInventory().addItem(kagura);

                    // Broadcast
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.sendMessage(Component.text(
                            "§5[MythicBlades] §f" + player.getName() +
                            " §5has forged the §fKagura no Tachi §5— Blade of the Divine Dance. §7One half screaming. One half silent.",
                            NamedTextColor.LIGHT_PURPLE));
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
                    }

                    cancel();
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
